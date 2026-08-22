package org.gridmind.backend.storage.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StorageLocationTest {

    @Test
    fun `storage location name must not be blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            StorageLocation(name = "   ")
        }

        assertEquals("Storage location name must not be blank.", exception.message)
    }
}
