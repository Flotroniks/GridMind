package org.gridmind.backend.category.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CategoryTest {

    @Test
    fun `category name must not be blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            Category(name = "   ")
        }

        assertEquals("Category name must not be blank.", exception.message)
    }
}
