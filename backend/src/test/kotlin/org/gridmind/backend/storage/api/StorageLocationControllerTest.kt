package org.gridmind.backend.storage.api

import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.shared.config.SecurityConfig
import org.gridmind.backend.shared.error.StorageLocationNotFoundException
import org.gridmind.backend.storage.application.StockAllocationService
import org.gridmind.backend.storage.application.StorageLocationService
import org.gridmind.backend.storage.domain.StorageLocation
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@WebMvcTest(StorageLocationController::class)
@Import(SecurityConfig::class)
class StorageLocationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var storageLocationService: StorageLocationService

    @MockitoBean
    private lateinit var stockAllocationService: StockAllocationService

    @MockitoBean
    private lateinit var inventoryService: InventoryService

    @Test
    fun `listLocations returns 200 with root locations`() {
        `when`(storageLocationService.findChildren(null))
            .thenReturn(listOf(StorageLocation(id = 1L, name = "Workshop")))
        `when`(storageLocationService.findChildren(1L)).thenReturn(emptyList())

        mockMvc.perform(get("/api/storage/locations"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Workshop"))
            .andExpect(jsonPath("$[0].hasChildren").value(false))
    }

    @Test
    fun `createLocation returns 201 with the created location`() {
        `when`(storageLocationService.create("Drawer 1", 1L))
            .thenReturn(StorageLocation(id = 2L, name = "Drawer 1", parentId = 1L))
        `when`(storageLocationService.findChildren(2L)).thenReturn(emptyList())

        mockMvc.perform(
            post("/api/storage/locations")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Drawer 1", "parentId" to 1L))),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Drawer 1"))
    }

    @Test
    fun `createLocation returns 404 for an unknown parent`() {
        `when`(storageLocationService.create("Drawer 1", 99L)).thenThrow(StorageLocationNotFoundException(99L))

        mockMvc.perform(
            post("/api/storage/locations")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Drawer 1", "parentId" to 99L))),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `renameLocation returns 200 with the renamed location`() {
        `when`(storageLocationService.rename(2L, "Capacitors"))
            .thenReturn(StorageLocation(id = 2L, name = "Capacitors", parentId = 1L))
        `when`(storageLocationService.findChildren(2L)).thenReturn(emptyList())

        mockMvc.perform(
            patch("/api/storage/locations/2")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Capacitors"))),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Capacitors"))
            .andExpect(jsonPath("$.id").value(2))
    }

    @Test
    fun `renameLocation returns 404 for an unknown location`() {
        `when`(storageLocationService.rename(99L, "Capacitors")).thenThrow(StorageLocationNotFoundException(99L))

        mockMvc.perform(
            patch("/api/storage/locations/99")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Capacitors"))),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `configureLed returns 200 with the led-mapped location`() {
        `when`(storageLocationService.configureLed(2L, "strip-a", 3))
            .thenReturn(StorageLocation(id = 2L, name = "Drawer 1", ledControllerId = "strip-a", ledIndex = 3))
        `when`(storageLocationService.findChildren(2L)).thenReturn(emptyList())

        mockMvc.perform(
            patch("/api/storage/locations/2/led")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("controllerId" to "strip-a", "index" to 3))),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ledControllerId").value("strip-a"))
            .andExpect(jsonPath("$.ledIndex").value(3))
    }

    @Test
    fun `deleteLocation returns 204`() {
        mockMvc.perform(delete("/api/storage/locations/1"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `getContents returns 200 with the location contents`() {
        `when`(storageLocationService.findById(1L)).thenReturn(StorageLocation(id = 1L, name = "Drawer A"))
        `when`(stockAllocationService.stockByLocation(1L)).thenReturn(emptyList())

        mockMvc.perform(get("/api/storage/locations/1/contents"))
            .andExpect(status().isOk)
    }
}
