package org.gridmind.backend.catalog.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CatalogResultTest {

    @Test
    fun `name must not be blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            CatalogResult(name = "   ", manufacturer = null)
        }

        assertEquals("Catalog result name must not be blank.", exception.message)
    }

    @Test
    fun `mpn is optional, for maker hardware without a real part number`() {
        val result = CatalogResult(name = "Wemos D1 Mini", manufacturer = "Wemos")

        assertNull(result.mpn)
    }

    @Test
    fun `mpn must not be blank when present`() {
        val exception = assertThrows<IllegalArgumentException> {
            CatalogResult(name = "Part", manufacturer = null, mpn = "  ")
        }

        assertEquals("Catalog result MPN must not be blank when present.", exception.message)
    }
}
