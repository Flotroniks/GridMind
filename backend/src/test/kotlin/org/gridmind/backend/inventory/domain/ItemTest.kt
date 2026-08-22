package org.gridmind.backend.inventory.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ItemTest {

    @Test
    fun `item name must not be blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            Item(name = "   ", quantity = 1)
        }

        assertEquals("Item name must not be blank.", exception.message)
    }

    @Test
    fun `item quantity must be zero or positive`() {
        val exception = assertThrows<IllegalArgumentException> {
            Item(name = "Resistor", quantity = -1)
        }

        assertEquals("Item quantity must be greater than or equal to 0.", exception.message)
    }

    @Test
    fun `item minimum quantity must be zero or positive`() {
        val exception = assertThrows<IllegalArgumentException> {
            Item(name = "Resistor", quantity = 1, minimumQuantity = -1)
        }

        assertEquals("Item minimum quantity must be greater than or equal to 0.", exception.message)
    }
}
