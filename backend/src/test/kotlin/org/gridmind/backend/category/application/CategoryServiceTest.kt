package org.gridmind.backend.category.application

import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.category.infrastructure.persistence.CategoryRepository
import org.gridmind.backend.shared.error.CategoryNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional

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

    @Test
    fun `rename updates the name when it's not taken by another category`() {
        val entity = CategoryEntity(id = 5L, name = "Sensors")
        `when`(categoryRepository.findById(5L)).thenReturn(Optional.of(entity))
        `when`(categoryRepository.existsByNameIgnoreCase("Motion sensors")).thenReturn(false)
        `when`(categoryRepository.save(entity)).thenReturn(entity)

        val renamed = categoryService.rename(5L, "Motion sensors")

        assertEquals("Motion sensors", renamed.name)
    }

    @Test
    fun `rename to the category's own current name (different case) is not treated as a duplicate`() {
        val entity = CategoryEntity(id = 5L, name = "Sensors")
        `when`(categoryRepository.findById(5L)).thenReturn(Optional.of(entity))
        `when`(categoryRepository.save(entity)).thenReturn(entity)

        val renamed = categoryService.rename(5L, "sensors")

        assertEquals("sensors", renamed.name)
    }

    @Test
    fun `rename rejects a name already used by a different category`() {
        val entity = CategoryEntity(id = 5L, name = "Sensors")
        `when`(categoryRepository.findById(5L)).thenReturn(Optional.of(entity))
        `when`(categoryRepository.existsByNameIgnoreCase("Motors")).thenReturn(true)

        assertThrows<IllegalArgumentException> { categoryService.rename(5L, "Motors") }
    }

    @Test
    fun `rename throws for an unknown category`() {
        `when`(categoryRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<CategoryNotFoundException> { categoryService.rename(99L, "Sensors") }
    }

    @Test
    fun `delete removes an existing category`() {
        `when`(categoryRepository.existsById(5L)).thenReturn(true)

        categoryService.delete(5L)

        verify(categoryRepository).deleteById(5L)
    }

    @Test
    fun `delete throws for an unknown category`() {
        `when`(categoryRepository.existsById(99L)).thenReturn(false)

        assertThrows<CategoryNotFoundException> { categoryService.delete(99L) }
    }
}
