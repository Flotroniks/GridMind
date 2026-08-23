package org.gridmind.backend.imageanalysis.application

import org.gridmind.backend.imageanalysis.domain.ImageAnalysisResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class ImageAnalysisServiceTest {

    @Test
    fun `delegates a valid image to the port and returns its result`() {
        val expected = ImageAnalysisResult(name = "D1 Mini", confidence = 0.8)
        val port = CountingPort(expected)
        val service = ImageAnalysisService(port)

        val result = service.analyze(validJpegBytes())

        assertEquals(expected, result)
        assertEquals(1, port.callCount)
    }

    @Test
    fun `rejects an invalid image before ever calling the port`() {
        val port = CountingPort(ImageAnalysisResult())
        val service = ImageAnalysisService(port)

        assertThrows<IllegalArgumentException> { service.analyze(ByteArray(0)) }

        assertEquals(0, port.callCount)
    }

    /** A plain fake rather than a mock: the only thing this test needs to know is
     * whether the port was called, which a call counter shows more plainly than a
     * mocking framework's argument-matcher machinery would for a single-method port. */
    private class CountingPort(private val result: ImageAnalysisResult) : ImageAnalysisPort {
        var callCount = 0
            private set

        override fun analyze(imageBytes: ByteArray): ImageAnalysisResult {
            callCount++
            return result
        }
    }

    private fun validJpegBytes(): ByteArray {
        val buffered = BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB)
        val out = ByteArrayOutputStream()
        ImageIO.write(buffered, "jpg", out)
        return out.toByteArray()
    }
}
