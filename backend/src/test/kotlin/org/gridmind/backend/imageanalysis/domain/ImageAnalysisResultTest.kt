package org.gridmind.backend.imageanalysis.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ImageAnalysisResultTest {

    @Test
    fun `every field is optional`() {
        val result = ImageAnalysisResult()

        assertNull(result.objectType)
        assertNull(result.name)
        assertNull(result.confidence)
        assertEquals(emptyList<String>(), result.visibleText)
        assertEquals(emptyList<String>(), result.searchQueries)
    }

    @Test
    fun `confidence must be between 0 and 1 when present`() {
        val exception = assertThrows<IllegalArgumentException> {
            ImageAnalysisResult(confidence = 1.5)
        }

        assertEquals("Confidence must be between 0.0 and 1.0.", exception.message)
    }

    @Test
    fun `a negative confidence is also rejected`() {
        assertThrows<IllegalArgumentException> {
            ImageAnalysisResult(confidence = -0.1)
        }
    }

    @Test
    fun `boundary confidence values are accepted`() {
        assertEquals(0.0, ImageAnalysisResult(confidence = 0.0).confidence)
        assertEquals(1.0, ImageAnalysisResult(confidence = 1.0).confidence)
    }
}
