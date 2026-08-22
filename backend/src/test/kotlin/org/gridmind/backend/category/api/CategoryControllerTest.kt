package org.gridmind.backend.category.api

import org.gridmind.backend.category.application.CategoryService
import org.gridmind.backend.category.domain.Category
import org.gridmind.backend.shared.config.SecurityConfig
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
}
