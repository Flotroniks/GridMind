package org.gridmind.backend.storage.api

import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.inventory.domain.Item
import org.gridmind.backend.shared.config.SecurityConfig
import org.gridmind.backend.shared.error.InsufficientStockException
import org.gridmind.backend.storage.application.StockAllocationService
import org.gridmind.backend.storage.application.StorageLocationService
import org.gridmind.backend.storage.domain.ItemStock
import org.gridmind.backend.storage.domain.StorageLocation
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@WebMvcTest(StockController::class)
@Import(SecurityConfig::class)
class StockControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var stockAllocationService: StockAllocationService

    @MockitoBean
    private lateinit var inventoryService: InventoryService

    @MockitoBean
    private lateinit var storageLocationService: StorageLocationService

    @Test
    fun `getStockForItem returns 200 with stock rows joined with names`() {
        `when`(stockAllocationService.stockByItem(1L))
            .thenReturn(listOf(ItemStock(itemId = 1L, storageLocationId = 2L, quantity = 4)))
        `when`(inventoryService.findById(1L)).thenReturn(Item(id = 1L, name = "ESP32-S3", quantity = 10))
        `when`(storageLocationService.findById(2L)).thenReturn(StorageLocation(id = 2L, name = "Drawer A"))

        mockMvc.perform(get("/api/storage/items/1/stock"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].itemName").value("ESP32-S3"))
            .andExpect(jsonPath("$[0].storageLocationName").value("Drawer A"))
    }

    @Test
    fun `allocate returns 200 with the allocation`() {
        `when`(stockAllocationService.allocate(1L, 2L, 4))
            .thenReturn(ItemStock(itemId = 1L, storageLocationId = 2L, quantity = 4))
        `when`(inventoryService.findById(1L)).thenReturn(Item(id = 1L, name = "ESP32-S3", quantity = 10))
        `when`(storageLocationService.findById(2L)).thenReturn(StorageLocation(id = 2L, name = "Drawer A"))

        mockMvc.perform(
            post("/api/storage/stock/allocate")
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(
                        mapOf("itemId" to 1L, "storageLocationId" to 2L, "quantity" to 4),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.quantity").value(4))
    }

    @Test
    fun `allocate returns 409 when stock is insufficient`() {
        `when`(stockAllocationService.allocate(1L, 2L, 100))
            .thenThrow(InsufficientStockException("not enough stock"))

        mockMvc.perform(
            post("/api/storage/stock/allocate")
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(
                        mapOf("itemId" to 1L, "storageLocationId" to 2L, "quantity" to 100),
                    ),
                ),
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `move returns 204 on success`() {
        mockMvc.perform(
            post("/api/storage/stock/move")
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(
                        mapOf("itemId" to 1L, "fromLocationId" to 2L, "toLocationId" to 3L, "quantity" to 2),
                    ),
                ),
        )
            .andExpect(status().isNoContent)
    }
}
