package org.gridmind.backend.backup.application

import java.time.Instant

/**
 * A full, portable snapshot of every entity in the database, plus the image bytes
 * themselves (base64) so the file is self-contained and doesn't depend on the media
 * volume it was exported from. Ids here are only meaningful *within this file* — they
 * let entries reference each other (e.g. an item's [ItemSnapshot.categoryId]) and are
 * remapped onto freshly-assigned ids on import, never reused as-is (see [BackupService.import]).
 */
data class BackupSnapshot(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Instant = Instant.now(),
    val categories: List<CategorySnapshot> = emptyList(),
    val storageLocations: List<StorageLocationSnapshot> = emptyList(),
    val images: List<ImageSnapshot> = emptyList(),
    val items: List<ItemSnapshot> = emptyList(),
    val itemStock: List<ItemStockSnapshot> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

data class CategorySnapshot(
    val id: Long,
    val name: String,
)

data class StorageLocationSnapshot(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val ledControllerId: String?,
    val ledIndex: Int?,
)

/** [contentBase64] is null when the original file couldn't be read at export time (e.g.
 * already missing from the media volume) — the metadata is still exported so nothing
 * else in the file has to know or care. */
data class ImageSnapshot(
    val id: Long,
    val checksum: String,
    val contentType: String,
    val fileName: String,
    val sourceProvider: String?,
    val sourceUrl: String?,
    val contentBase64: String?,
)

data class ItemSnapshot(
    val id: Long,
    val name: String,
    val quantity: Int,
    val description: String?,
    val manufacturer: String?,
    val reference: String?,
    val categoryId: Long?,
    val tags: List<String>,
    val notes: String?,
    val productUrl: String?,
    val datasheetUrl: String?,
    val minimumQuantity: Int,
    val quantityHs: Int,
    val quantityInUse: Int,
    val imageId: Long?,
)

data class ItemStockSnapshot(
    val id: Long,
    val itemId: Long,
    val storageLocationId: Long,
    val quantity: Int,
)

/** Counts of what actually got imported, for the admin UI to report back. */
data class BackupSummary(
    val categories: Int,
    val storageLocations: Int,
    val images: Int,
    val items: Int,
    val itemStock: Int,
)
