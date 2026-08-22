package org.gridmind.backend.inventory.api

import org.gridmind.backend.category.application.CategoryService
import org.gridmind.backend.category.domain.Category
import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.inventory.domain.Item
import org.gridmind.backend.shared.config.SecurityConfig
import org.gridmind.backend.shared.error.ItemNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
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

@WebMvcTest(ItemController::class)
@Import(SecurityConfig::class)
class ItemControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var inventoryService: InventoryService

    @MockitoBean
    private lateinit var categoryService: CategoryService

    @Test
    fun `create returns 201 with the created item`() {
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(inventoryService.create(Item(name = "ESP32-S3", quantity = 4)))
            .thenReturn(Item(id = 1L, name = "ESP32-S3", quantity = 4))

        mockMvc.perform(
            post("/api/inventory/items")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "ESP32-S3", "quantity" to 4))),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("ESP32-S3"))
    }

    @Test
    fun `create returns 400 when name is blank`() {
        mockMvc.perform(
            post("/api/inventory/items")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "", "quantity" to 4))),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors.name").exists())
    }

    @Test
    fun `getItem returns 200 with the item`() {
        `when`(categoryService.findAll()).thenReturn(listOf(Category(id = 2L, name = "Modules")))
        `when`(inventoryService.findById(1L))
            .thenReturn(Item(id = 1L, name = "ESP32-S3", quantity = 4, categoryId = 2L))

        mockMvc.perform(get("/api/inventory/items/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("ESP32-S3"))
            .andExpect(jsonPath("$.categoryName").value("Modules"))
    }

    @Test
    fun `getItem returns 404 for an unknown id`() {
        `when`(inventoryService.findById(99L)).thenThrow(ItemNotFoundException(99L))

        mockMvc.perform(get("/api/inventory/items/99"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `listItems passes search params through`() {
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(inventoryService.search("esp", 2L, "Espressif")).thenReturn(emptyList())

        mockMvc.perform(get("/api/inventory/items?search=esp&categoryId=2&manufacturer=Espressif"))
            .andExpect(status().isOk)
    }

    @Test
    fun `updateItem returns 200 with the updated item`() {
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(inventoryService.update(1L, Item(name = "Updated", quantity = 9)))
            .thenReturn(Item(id = 1L, name = "Updated", quantity = 9))

        mockMvc.perform(
            patch("/api/inventory/items/1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Updated", "quantity" to 9))),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }

    @Test
    fun `deleteItem returns 204`() {
        mockMvc.perform(delete("/api/inventory/items/1"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `deleteItem returns 404 for an unknown id`() {
        doThrow(ItemNotFoundException(99L)).`when`(inventoryService).delete(99L)

        mockMvc.perform(delete("/api/inventory/items/99"))
            .andExpect(status().isNotFound)
    }
}
