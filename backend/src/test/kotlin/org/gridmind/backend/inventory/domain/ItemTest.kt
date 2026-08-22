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

    @Test
    fun `item hs quantity must be zero or positive`() {
        val exception = assertThrows<IllegalArgumentException> {
            Item(name = "Resistor", quantity = 1, quantityHs = -1)
        }

        assertEquals("Item HS quantity must be greater than or equal to 0.", exception.message)
    }

    @Test
    fun `item in-use quantity must be zero or positive`() {
        val exception = assertThrows<IllegalArgumentException> {
            Item(name = "Resistor", quantity = 1, quantityInUse = -1)
        }

        assertEquals("Item in-use quantity must be greater than or equal to 0.", exception.message)
    }

    @Test
    fun `item hs and in-use quantities must not exceed total quantity`() {
        val exception = assertThrows<IllegalArgumentException> {
            Item(name = "ESP32-S3", quantity = 5, quantityHs = 3, quantityInUse = 3)
        }

        assertEquals("Item HS and in-use quantities must not exceed total quantity.", exception.message)
    }

    @Test
    fun `item computes available quantity`() {
        val item = Item(name = "ESP32-S3", quantity = 5, quantityHs = 1, quantityInUse = 2)

        assertEquals(2, item.quantityAvailable)
    }
}
