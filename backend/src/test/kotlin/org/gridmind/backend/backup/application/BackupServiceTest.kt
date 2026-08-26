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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.anyIterable
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong

class BackupServiceTest {

    private val categoryRepository: CategoryRepository = mock(CategoryRepository::class.java)
    private val storageLocationRepository: StorageLocationRepository = mock(StorageLocationRepository::class.java)
    private val itemRepository: ItemRepository = mock(ItemRepository::class.java)
    private val itemStockRepository: ItemStockRepository = mock(ItemStockRepository::class.java)
    private val storedImageRepository: StoredImageRepository = mock(StoredImageRepository::class.java)

    @TempDir
    lateinit var mediaDir: Path

    private lateinit var service: BackupService
    private val nextId = AtomicLong(100)

    @BeforeEach
    fun setUp() {
        service = BackupService(
            categoryRepository,
            storageLocationRepository,
            itemRepository,
            itemStockRepository,
            storedImageRepository,
            mediaDir.toString(),
        )

        `when`(categoryRepository.save(any(CategoryEntity::class.java))).thenAnswer { inv ->
            (inv.arguments[0] as CategoryEntity).also { it.id = nextId.incrementAndGet() }
        }
        `when`(storageLocationRepository.save(any(StorageLocationEntity::class.java))).thenAnswer { inv ->
            (inv.arguments[0] as StorageLocationEntity).also { it.id = nextId.incrementAndGet() }
        }
        `when`(itemRepository.save(any(ItemEntity::class.java))).thenAnswer { inv ->
            (inv.arguments[0] as ItemEntity).also { it.id = nextId.incrementAndGet() }
        }
        `when`(itemStockRepository.save(any(ItemStockEntity::class.java))).thenAnswer { inv ->
            (inv.arguments[0] as ItemStockEntity).also { it.id = nextId.incrementAndGet() }
        }
        `when`(storedImageRepository.save(any(StoredImageEntity::class.java))).thenAnswer { inv ->
            (inv.arguments[0] as StoredImageEntity).also { it.id = nextId.incrementAndGet() }
        }
        `when`(categoryRepository.getReferenceById(anyLong()))
            .thenAnswer { inv -> CategoryEntity(id = inv.arguments[0] as Long, name = "x") }
        `when`(storageLocationRepository.getReferenceById(anyLong()))
            .thenAnswer { inv -> StorageLocationEntity(id = inv.arguments[0] as Long, name = "x") }
        `when`(itemRepository.getReferenceById(anyLong()))
            .thenAnswer { inv -> ItemEntity(id = inv.arguments[0] as Long, name = "x", quantity = 0) }
        `when`(storageLocationRepository.count()).thenReturn(0L)
    }

    @Test
    fun `export maps every repository into the snapshot`() {
        `when`(categoryRepository.findAll()).thenReturn(listOf(CategoryEntity(id = 1L, name = "Sensors")))
        `when`(storageLocationRepository.findAll()).thenReturn(
            listOf(StorageLocationEntity(id = 2L, name = "Workshop", ledControllerId = "strip-a", ledIndex = 3)),
        )
        `when`(storedImageRepository.findAll()).thenReturn(emptyList())
        val category = CategoryEntity(id = 1L, name = "Sensors")
        val item = ItemEntity(id = 3L, name = "ESP32", quantity = 5, category = category, tags = mutableListOf("wifi"))
        `when`(itemRepository.findAll()).thenReturn(listOf(item))
        `when`(itemStockRepository.findAll()).thenReturn(emptyList())

        val snapshot = service.export()

        assertEquals(1, snapshot.categories.size)
        assertEquals("Sensors", snapshot.categories[0].name)
        assertEquals("strip-a", snapshot.storageLocations[0].ledControllerId)
        assertEquals(3, snapshot.storageLocations[0].ledIndex)
        assertEquals(listOf("wifi"), snapshot.items[0].tags)
        assertEquals(1L, snapshot.items[0].categoryId)
    }

    @Test
    fun `import clears existing data before restoring`() {
        service.import(BackupSnapshot())

        verify(itemRepository, times(1)).deleteAll()
        verify(itemRepository, times(1)).flush()
        verify(storedImageRepository, times(1)).deleteAll()
        verify(categoryRepository, times(1)).deleteAll()
    }

    @Test
    fun `import remaps ids across categories, items and stock`() {
        val snapshot = BackupSnapshot(
            categories = listOf(CategorySnapshot(id = 1L, name = "Sensors")),
            storageLocations = listOf(
                StorageLocationSnapshot(id = 10L, name = "Workshop", parentId = null, ledControllerId = null, ledIndex = null),
            ),
            items = listOf(
                ItemSnapshot(
                    id = 5L,
                    name = "ESP32",
                    quantity = 5,
                    description = null,
                    manufacturer = null,
                    reference = null,
                    categoryId = 1L,
                    tags = emptyList(),
                    notes = null,
                    productUrl = null,
                    datasheetUrl = null,
                    minimumQuantity = 0,
                    quantityHs = 0,
                    quantityInUse = 0,
                    imageId = null,
                ),
            ),
            itemStock = listOf(ItemStockSnapshot(id = 20L, itemId = 5L, storageLocationId = 10L, quantity = 2)),
        )

        val summary = service.import(snapshot)

        assertEquals(1, summary.categories)
        assertEquals(1, summary.storageLocations)
        assertEquals(1, summary.items)
        assertEquals(1, summary.itemStock)
        verify(itemStockRepository, times(1)).save(any(ItemStockEntity::class.java))
    }

    @Test
    fun `import rejects a backup file newer than this server supports`() {
        val snapshot = BackupSnapshot(version = BackupSnapshot.CURRENT_VERSION + 1)

        assertThrows<IllegalArgumentException> { service.import(snapshot) }
    }

    @Test
    fun `import skips a storage location with a dangling parent reference`() {
        val snapshot = BackupSnapshot(
            storageLocations = listOf(
                StorageLocationSnapshot(id = 1L, name = "Orphan", parentId = 999L, ledControllerId = null, ledIndex = null),
            ),
        )

        val summary = service.import(snapshot)

        assertEquals(0, summary.storageLocations)
    }

    @Test
    fun `clear deletes storage locations leaf-first`() {
        `when`(storageLocationRepository.count()).thenReturn(2L, 1L, 0L)
        `when`(storageLocationRepository.findLeafIds()).thenReturn(listOf(2L), listOf(1L))

        service.clear()

        verify(storageLocationRepository, times(2)).deleteAllByIdInBatch(anyIterable())
    }
}
