package org.gridmind.backend.backup.application

import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.category.infrastructure.persistence.CategoryRepository
import org.gridmind.backend.inventory.infrastructure.persistence.ItemEntity
import org.gridmind.backend.inventory.infrastructure.persistence.ItemRepository
import org.gridmind.backend.inventory.infrastructure.persistence.StoredImageEntity
import org.gridmind.backend.inventory.infrastructure.persistence.StoredImageRepository
import org.gridmind.backend.storage.infrastructure.persistence.ItemStockEntity
import org.gridmind.backend.storage.infrastructure.persistence.ItemStockRepository
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationEntity
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.Base64

/**
 * Exports and restores the entire database as one portable JSON snapshot, and clears it
 * back to empty. This exists so the physical layout — items, categories, storage
 * locations, stock, images — can be backed up and moved around independently of any one
 * running instance, and so a bad state can be wiped and rebuilt from a known-good file.
 *
 * Import always replaces whatever's currently there (see [import]): every id in the
 * snapshot is treated as a label local to that file, remapped onto freshly-assigned ids
 * as rows are inserted, never reused as-is — which sidesteps every uniqueness/FK
 * conflict a partial or id-colliding restore would otherwise hit.
 */
@Service
class BackupService(
    private val categoryRepository: CategoryRepository,
    private val storageLocationRepository: StorageLocationRepository,
    private val itemRepository: ItemRepository,
    private val itemStockRepository: ItemStockRepository,
    private val storedImageRepository: StoredImageRepository,
    @Value("\${gridmind.media.storage-path:./data/media}") storagePath: String,
) {
    private val logger = LoggerFactory.getLogger(BackupService::class.java)
    private val storageDir: Path = Path.of(storagePath)

    @Transactional(readOnly = true)
    fun export(): BackupSnapshot = BackupSnapshot(
        exportedAt = Instant.now(),
        categories = categoryRepository.findAll().map { CategorySnapshot(id = it.id!!, name = it.name) },
        storageLocations = storageLocationRepository.findAll().map {
            StorageLocationSnapshot(
                id = it.id!!,
                name = it.name,
                parentId = it.parent?.id,
                ledControllerId = it.ledControllerId,
                ledIndex = it.ledIndex,
            )
        },
        images = storedImageRepository.findAll().map(::toImageSnapshot),
        items = itemRepository.findAll().map {
            ItemSnapshot(
                id = it.id!!,
                name = it.name,
                quantity = it.quantity,
                description = it.description,
                manufacturer = it.manufacturer,
                reference = it.reference,
                categoryId = it.category?.id,
                tags = it.tags.toList(),
                notes = it.notes,
                productUrl = it.productUrl,
                datasheetUrl = it.datasheetUrl,
                minimumQuantity = it.minimumQuantity,
                quantityHs = it.quantityHs,
                quantityInUse = it.quantityInUse,
                imageId = it.imageId,
            )
        },
        itemStock = itemStockRepository.findAll().map {
            ItemStockSnapshot(id = it.id!!, itemId = it.item.id!!, storageLocationId = it.storageLocation.id!!, quantity = it.quantity)
        },
    )

    @Transactional
    fun import(snapshot: BackupSnapshot): BackupSummary {
        require(snapshot.version <= BackupSnapshot.CURRENT_VERSION) {
            "Backup file version ${snapshot.version} is newer than this server supports (${BackupSnapshot.CURRENT_VERSION})."
        }

        clearAll()

        val categoryIds = snapshot.categories.associate { it.id to categoryRepository.save(CategoryEntity(name = it.name)).id!! }
        val locationIds = importStorageLocations(snapshot.storageLocations)
        val imageIds = snapshot.images.associate { it.id to importImage(it).id!! }

        val itemIds = snapshot.items.associate { snap ->
            val category = snap.categoryId?.let { categoryIds[it] }?.let { categoryRepository.getReferenceById(it) }
            val entity = ItemEntity(
                name = snap.name,
                quantity = snap.quantity,
                description = snap.description,
                manufacturer = snap.manufacturer,
                reference = snap.reference,
                category = category,
                tags = snap.tags.toMutableList(),
                notes = snap.notes,
                productUrl = snap.productUrl,
                datasheetUrl = snap.datasheetUrl,
                minimumQuantity = snap.minimumQuantity,
                quantityHs = snap.quantityHs,
                quantityInUse = snap.quantityInUse,
                imageId = snap.imageId?.let { imageIds[it] },
            )
            snap.id to itemRepository.save(entity).id!!
        }

        var stockRestored = 0
        snapshot.itemStock.forEach { snap ->
            val itemId = itemIds[snap.itemId]
            val locationId = locationIds[snap.storageLocationId]
            if (itemId == null || locationId == null) {
                logger.warn("Skipping item-stock entry referencing an item/location missing from the backup file.")
                return@forEach
            }
            itemStockRepository.save(
                ItemStockEntity(
                    item = itemRepository.getReferenceById(itemId),
                    storageLocation = storageLocationRepository.getReferenceById(locationId),
                    quantity = snap.quantity,
                ),
            )
            stockRestored++
        }

        return BackupSummary(
            categories = categoryIds.size,
            storageLocations = locationIds.size,
            images = imageIds.size,
            items = itemIds.size,
            itemStock = stockRestored,
        )
    }

    /** Wipes every table this module knows about, leaving the schema itself untouched.
     * Deliberately doesn't touch files already on the media volume — an unreferenced
     * image file left behind is harmless, and re-importing later just overwrites it. */
    @Transactional
    fun clear() = clearAll()

    private fun clearAll() {
        // deleteAll() only queues deletes in the persistence context — each is flushed
        // immediately so the DB genuinely reflects it before the next step depends on
        // that (the leaf-first location wipe needs item_stock's cascade-deleted rows to
        // actually be gone, and import()'s inserts right after need every unique-name/
        // -checksum row genuinely cleared, not just pending).
        itemRepository.deleteAll()
        itemRepository.flush()
        clearStorageLocationsLeafFirst()
        storedImageRepository.deleteAll()
        storedImageRepository.flush()
        categoryRepository.deleteAll()
        categoryRepository.flush()
    }

    /** storage_locations.parent_id is `ON DELETE RESTRICT`, and unlike a `CASCADE`
     * target that constraint is checked immediately per row — a single bulk delete over
     * a multi-level hierarchy would fail on whichever parent Postgres happens to process
     * before its children. Deleting one leaf layer at a time sidesteps that entirely. */
    private fun clearStorageLocationsLeafFirst() {
        while (storageLocationRepository.count() > 0) {
            val leafIds = storageLocationRepository.findLeafIds()
            if (leafIds.isEmpty()) break
            storageLocationRepository.deleteAllByIdInBatch(leafIds)
        }
    }

    /** Inserts locations top-down regardless of the snapshot's own ordering: repeatedly
     * inserts whatever's currently insertable (no parent, or a parent already inserted)
     * until nothing is left, so an arbitrarily deep/out-of-order hierarchy restores correctly. */
    private fun importStorageLocations(locations: List<StorageLocationSnapshot>): Map<Long, Long> {
        val idMap = mutableMapOf<Long, Long>()
        var remaining = locations
        while (remaining.isNotEmpty()) {
            val (ready, notReady) = remaining.partition { it.parentId == null || idMap.containsKey(it.parentId) }
            if (ready.isEmpty()) {
                logger.warn("Skipping {} storage location(s) with a dangling or cyclic parent reference.", remaining.size)
                break
            }
            ready.forEach { snap ->
                val parent = snap.parentId?.let { idMap[it] }?.let { storageLocationRepository.getReferenceById(it) }
                val entity = StorageLocationEntity(
                    name = snap.name,
                    parent = parent,
                    ledControllerId = snap.ledControllerId,
                    ledIndex = snap.ledIndex,
                )
                idMap[snap.id] = storageLocationRepository.save(entity).id!!
            }
            remaining = notReady
        }
        return idMap
    }

    private fun importImage(snapshot: ImageSnapshot): StoredImageEntity {
        if (snapshot.contentBase64 != null) {
            Files.createDirectories(storageDir)
            Files.write(storageDir.resolve(snapshot.fileName), Base64.getDecoder().decode(snapshot.contentBase64))
        }
        return storedImageRepository.save(
            StoredImageEntity(
                checksum = snapshot.checksum,
                contentType = snapshot.contentType,
                filePath = storageDir.resolve(snapshot.fileName).toString(),
                sourceProvider = snapshot.sourceProvider,
                sourceUrl = snapshot.sourceUrl,
            ),
        )
    }

    private fun toImageSnapshot(image: StoredImageEntity): ImageSnapshot {
        val bytes = runCatching { Files.readAllBytes(Path.of(image.filePath)) }
            .onFailure { logger.warn("Could not read image file '{}' for export: {}", image.filePath, it.message) }
            .getOrNull()
        return ImageSnapshot(
            id = image.id!!,
            checksum = image.checksum,
            contentType = image.contentType,
            fileName = Path.of(image.filePath).fileName.toString(),
            sourceProvider = image.sourceProvider,
            sourceUrl = image.sourceUrl,
            contentBase64 = bytes?.let { Base64.getEncoder().encodeToString(it) },
        )
    }
}
