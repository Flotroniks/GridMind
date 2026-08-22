package org.gridmind.backend.category.application

import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.category.infrastructure.persistence.CategoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class CategoryServiceTest {

    private val categoryRepository: CategoryRepository = mock(CategoryRepository::class.java)
    private val categoryService = CategoryService(categoryRepository)

    @Test
    fun `service creates a category with a unique name`() {
        `when`(categoryRepository.existsByNameIgnoreCase("Sensors")).thenReturn(false)
        `when`(categoryRepository.save(any(CategoryEntity::class.java))).thenAnswer { invocation ->
            val entity = invocation.arguments[0] as CategoryEntity
            entity.id = 5L
            entity
        }

        val created = categoryService.create("Sensors")

        assertEquals("Sensors", created.name)
        assertEquals(5L, created.id)
    }

    @Test
    fun `service rejects a duplicate category name`() {
        `when`(categoryRepository.existsByNameIgnoreCase("Sensors")).thenReturn(true)

        assertThrows<IllegalArgumentException> { categoryService.create("Sensors") }
    }
}
