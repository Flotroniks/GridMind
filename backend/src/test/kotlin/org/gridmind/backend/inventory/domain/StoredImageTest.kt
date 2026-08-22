package org.gridmind.backend.inventory.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StoredImageTest {

    @Test
    fun `checksum must not be blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            StoredImage(checksum = "   ", contentType = "image/png", filePath = "/data/media/a.png")
        }

        assertEquals("Stored image checksum must not be blank.", exception.message)
    }

    @Test
    fun `content type must not be blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            StoredImage(checksum = "abc123", contentType = " ", filePath = "/data/media/a.png")
        }

        assertEquals("Stored image content type must not be blank.", exception.message)
    }

    @Test
    fun `file path must not be blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            StoredImage(checksum = "abc123", contentType = "image/png", filePath = "")
        }

        assertEquals("Stored image file path must not be blank.", exception.message)
    }
}
