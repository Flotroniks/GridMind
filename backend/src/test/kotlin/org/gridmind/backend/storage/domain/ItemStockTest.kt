package org.gridmind.backend.storage.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ItemStockTest {

    @Test
    fun `item stock quantity must be zero or positive`() {
        val exception = assertThrows<IllegalArgumentException> {
            ItemStock(itemId = 1L, storageLocationId = 1L, quantity = -1)
        }

        assertEquals("Item stock quantity must be greater than or equal to 0.", exception.message)
    }
}
