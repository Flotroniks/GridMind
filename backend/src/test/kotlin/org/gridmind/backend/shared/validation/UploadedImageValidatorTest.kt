package org.gridmind.backend.shared.validation

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class UploadedImageValidatorTest {

    @Test
    fun `accepts a valid JPEG`() {
        assertDoesNotThrow { UploadedImageValidator.validate(image("jpg")) }
    }

    @Test
    fun `accepts a valid PNG`() {
        assertDoesNotThrow { UploadedImageValidator.validate(image("png")) }
    }

    @Test
    fun `rejects an empty file`() {
        val exception = assertThrows<IllegalArgumentException> {
            UploadedImageValidator.validate(ByteArray(0))
        }

        assertEquals("The uploaded file is empty.", exception.message)
    }

    @Test
    fun `rejects a file over the maximum size`() {
        val tooBig = ByteArray(10 * 1024 * 1024 + 1)

        val exception = assertThrows<IllegalArgumentException> {
            UploadedImageValidator.validate(tooBig)
        }

        assertEquals("The uploaded file exceeds the maximum allowed size (10 MiB).", exception.message)
    }

    @Test
    fun `rejects a format that isn't JPEG or PNG, even though ImageIO can decode it`() {
        val exception = assertThrows<IllegalArgumentException> {
            UploadedImageValidator.validate(image("gif"))
        }

        assertEquals("Unsupported image format. Accepted formats: JPEG, PNG.", exception.message)
    }

    @Test
    fun `rejects bytes that aren't a real image at all`() {
        val exception = assertThrows<IllegalArgumentException> {
            UploadedImageValidator.validate("this is definitely not an image".toByteArray())
        }

        assertEquals("Unsupported image format. Accepted formats: JPEG, PNG.", exception.message)
    }

    @Test
    fun `rejects a truncated, corrupted JPEG`() {
        val truncated = image("jpg").copyOf(20)

        val exception = assertThrows<IllegalArgumentException> {
            UploadedImageValidator.validate(truncated)
        }

        assertEquals("The file is not a valid image, or it is corrupted.", exception.message)
    }

    private fun image(format: String): ByteArray {
        val buffered = BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB)
        val out = ByteArrayOutputStream()
        ImageIO.write(buffered, format, out)
        return out.toByteArray()
    }
}
