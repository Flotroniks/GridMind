package org.gridmind.backend.category.api

import org.gridmind.backend.category.application.CategoryService
import org.gridmind.backend.category.domain.Category
import org.gridmind.backend.shared.config.SecurityConfig
import org.gridmind.backend.shared.error.CategoryNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
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

@WebMvcTest(CategoryController::class)
@Import(SecurityConfig::class)
class CategoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var categoryService: CategoryService

    @Test
    fun `listCategories returns 200 with all categories`() {
        `when`(categoryService.findAll()).thenReturn(listOf(Category(id = 1L, name = "Sensors")))

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Sensors"))
    }

    @Test
    fun `createCategory returns 201 with the created category`() {
        `when`(categoryService.create("Sensors")).thenReturn(Category(id = 1L, name = "Sensors"))

        mockMvc.perform(
            post("/api/categories")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Sensors"))),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Sensors"))
    }

    @Test
    fun `createCategory returns 400 for a duplicate name`() {
        `when`(categoryService.create("Sensors"))
            .thenThrow(IllegalArgumentException("Category 'Sensors' already exists."))

        mockMvc.perform(
            post("/api/categories")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Sensors"))),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `renameCategory returns 200 with the renamed category`() {
        `when`(categoryService.rename(1L, "Motion sensors"))
            .thenReturn(Category(id = 1L, name = "Motion sensors"))

        mockMvc.perform(
            patch("/api/categories/1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Motion sensors"))),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Motion sensors"))
    }

    @Test
    fun `renameCategory returns 404 for an unknown category`() {
        `when`(categoryService.rename(99L, "Sensors")).thenThrow(CategoryNotFoundException(99L))

        mockMvc.perform(
            patch("/api/categories/99")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Sensors"))),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `deleteCategory returns 204`() {
        mockMvc.perform(delete("/api/categories/1"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `deleteCategory returns 404 for an unknown category`() {
        doThrow(CategoryNotFoundException(99L)).`when`(categoryService).delete(99L)

        mockMvc.perform(delete("/api/categories/99"))
            .andExpect(status().isNotFound)
    }
}
