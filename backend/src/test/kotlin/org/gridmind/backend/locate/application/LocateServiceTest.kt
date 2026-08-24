package org.gridmind.backend.locate.application

import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.inventory.domain.Item
import org.gridmind.backend.locate.domain.LocateHighlight
import org.gridmind.backend.storage.application.StockAllocationService
import org.gridmind.backend.storage.application.StorageLocationService
import org.gridmind.backend.storage.domain.ItemStock
import org.gridmind.backend.storage.domain.StorageLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class LocateServiceTest {

    private val inventoryService: InventoryService = mock(InventoryService::class.java)
    private val stockAllocationService: StockAllocationService = mock(StockAllocationService::class.java)
    private val storageLocationService: StorageLocationService = mock(StorageLocationService::class.java)
    private val publisher = RecordingPublisher()

    private val service = LocateService(
        inventoryService,
        stockAllocationService,
        storageLocationService,
        publisher,
        "#39FF14",
    )

    @Test
    fun `publishes a highlight for every distinct location holding a matching item`() {
        val esp32 = Item(id = 1L, name = "ESP32-S3", quantity = 5)
        `when`(inventoryService.search("esp32", null, null)).thenReturn(listOf(esp32))
        `when`(stockAllocationService.stockByItem(1L)).thenReturn(
            listOf(
                ItemStock(itemId = 1L, storageLocationId = 10L, quantity = 3),
                ItemStock(itemId = 1L, storageLocationId = 11L, quantity = 2),
            ),
        )
        `when`(storageLocationService.findById(10L)).thenReturn(StorageLocation(id = 10L, name = "Tiroir A"))
        `when`(storageLocationService.findById(11L)).thenReturn(StorageLocation(id = 11L, name = "Tiroir B"))

        service.locate("esp32")

        assertEquals(
            setOf(
                LocateHighlight(storageLocationId = 10L, storageLocationName = "Tiroir A", color = "#39FF14"),
                LocateHighlight(storageLocationId = 11L, storageLocationName = "Tiroir B", color = "#39FF14"),
            ),
            publisher.lastPublished?.toSet(),
        )
    }

    @Test
    fun `deduplicates a location shared by two matching items`() {
        val itemA = Item(id = 1L, name = "ESP32-S3", quantity = 5)
        val itemB = Item(id = 2L, name = "ESP32-C3", quantity = 5)
        `when`(inventoryService.search("esp32", null, null)).thenReturn(listOf(itemA, itemB))
        `when`(stockAllocationService.stockByItem(1L)).thenReturn(
            listOf(ItemStock(itemId = 1L, storageLocationId = 10L, quantity = 1)),
        )
        `when`(stockAllocationService.stockByItem(2L)).thenReturn(
            listOf(ItemStock(itemId = 2L, storageLocationId = 10L, quantity = 1)),
        )
        `when`(storageLocationService.findById(10L)).thenReturn(StorageLocation(id = 10L, name = "Tiroir A"))

        service.locate("esp32")

        assertEquals(1, publisher.lastPublished?.size)
    }

    @Test
    fun `ignores a location whose stock for the matching item has been emptied to zero`() {
        val esp32 = Item(id = 1L, name = "ESP32-S3", quantity = 5)
        `when`(inventoryService.search("esp32", null, null)).thenReturn(listOf(esp32))
        `when`(stockAllocationService.stockByItem(1L)).thenReturn(
            listOf(
                ItemStock(itemId = 1L, storageLocationId = 10L, quantity = 0),
                ItemStock(itemId = 1L, storageLocationId = 11L, quantity = 2),
            ),
        )
        `when`(storageLocationService.findById(11L)).thenReturn(StorageLocation(id = 11L, name = "Tiroir B"))

        service.locate("esp32")

        assertEquals(
            listOf(LocateHighlight(storageLocationId = 11L, storageLocationName = "Tiroir B", color = "#39FF14")),
            publisher.lastPublished,
        )
    }

    @Test
    fun `publishes an empty highlight list for a blank query, without searching`() {
        service.locate("   ")

        assertTrue(publisher.lastPublished?.isEmpty() == true)
        assertEquals(1, publisher.callCount)
        verify(inventoryService, never()).search(any(), any(), any())
    }

    /** A plain fake rather than a mock: what these tests need to assert is the exact
     * final published state, which reading a captured list shows more plainly than a
     * mocking framework's argument-captor machinery would here. */
    private class RecordingPublisher : LocatePublisherPort {
        var lastPublished: List<LocateHighlight>? = null
            private set
        var callCount = 0
            private set

        override fun publish(highlights: List<LocateHighlight>) {
            lastPublished = highlights
            callCount++
        }

        override fun isConnected(): Boolean = true
    }
}
