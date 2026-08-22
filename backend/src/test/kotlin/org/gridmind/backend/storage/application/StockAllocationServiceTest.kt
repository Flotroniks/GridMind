package org.gridmind.backend.storage.application

import org.gridmind.backend.inventory.infrastructure.persistence.ItemEntity
import org.gridmind.backend.inventory.infrastructure.persistence.ItemRepository
import org.gridmind.backend.shared.error.InsufficientStockException
import org.gridmind.backend.shared.error.StorageLocationNotFoundException
import org.gridmind.backend.storage.infrastructure.persistence.ItemStockEntity
import org.gridmind.backend.storage.infrastructure.persistence.ItemStockRepository
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationEntity
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional

class StockAllocationServiceTest {

    private val itemStockRepository: ItemStockRepository = mock(ItemStockRepository::class.java)
    private val itemRepository: ItemRepository = mock(ItemRepository::class.java)
    private val storageLocationRepository: StorageLocationRepository = mock(StorageLocationRepository::class.java)
    private val stockAllocationService =
        StockAllocationService(itemStockRepository, itemRepository, storageLocationRepository)

    private val item = ItemEntity(id = 1L, name = "ESP32-S3", quantity = 10)
    private val locationA = StorageLocationEntity(id = 1L, name = "Drawer A")
    private val locationB = StorageLocationEntity(id = 2L, name = "Drawer B")

    @Test
    fun `allocate succeeds within available quantity`() {
        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(item))
        `when`(storageLocationRepository.findById(1L)).thenReturn(Optional.of(locationA))
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 1L)).thenReturn(null)
        `when`(itemStockRepository.sumQuantityByItemId(1L)).thenReturn(0)
        `when`(itemStockRepository.save(any(ItemStockEntity::class.java))).thenAnswer { it.arguments[0] }

        val result = stockAllocationService.allocate(1L, 1L, 6)

        assertEquals(6, result.quantity)
    }

    @Test
    fun `allocate rejects a quantity exceeding available stock`() {
        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(item))
        `when`(storageLocationRepository.findById(1L)).thenReturn(Optional.of(locationA))
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 1L)).thenReturn(null)
        `when`(itemStockRepository.sumQuantityByItemId(1L)).thenReturn(6)

        assertThrows<InsufficientStockException> { stockAllocationService.allocate(1L, 1L, 5) }
    }

    @Test
    fun `moveStock moves quantity from source to destination atomically`() {
        val source = ItemStockEntity(id = 10L, item = item, storageLocation = locationA, quantity = 6)
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 1L)).thenReturn(source)
        `when`(storageLocationRepository.findById(2L)).thenReturn(Optional.of(locationB))
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 2L)).thenReturn(null)
        `when`(itemStockRepository.save(any(ItemStockEntity::class.java))).thenAnswer { it.arguments[0] }

        stockAllocationService.moveStock(1L, 1L, 2L, 4)

        assertEquals(2, source.quantity)
        verify(itemStockRepository, times(2)).save(any(ItemStockEntity::class.java))
    }

    @Test
    fun `moveStock deletes the source row when fully moved`() {
        val source = ItemStockEntity(id = 10L, item = item, storageLocation = locationA, quantity = 4)
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 1L)).thenReturn(source)
        `when`(storageLocationRepository.findById(2L)).thenReturn(Optional.of(locationB))
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 2L)).thenReturn(null)
        `when`(itemStockRepository.save(any(ItemStockEntity::class.java))).thenAnswer { it.arguments[0] }

        stockAllocationService.moveStock(1L, 1L, 2L, 4)

        verify(itemStockRepository, times(1)).delete(source)
    }

    @Test
    fun `moveStock rejects insufficient source stock`() {
        val source = ItemStockEntity(id = 10L, item = item, storageLocation = locationA, quantity = 2)
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 1L)).thenReturn(source)

        assertThrows<InsufficientStockException> { stockAllocationService.moveStock(1L, 1L, 2L, 5) }
    }

    @Test
    fun `moveStock rejects an unknown destination location`() {
        val source = ItemStockEntity(id = 10L, item = item, storageLocation = locationA, quantity = 6)
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 1L)).thenReturn(source)
        `when`(storageLocationRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<StorageLocationNotFoundException> { stockAllocationService.moveStock(1L, 1L, 99L, 4) }
    }

    @Test
    fun `moveStock rejects when there is no stock at the source`() {
        `when`(itemStockRepository.findByItemIdAndStorageLocationId(1L, 1L)).thenReturn(null)

        assertThrows<InsufficientStockException> { stockAllocationService.moveStock(1L, 1L, 2L, 1) }
    }
}
