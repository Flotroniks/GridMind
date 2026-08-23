package org.gridmind.backend.imageanalysis.infrastructure.ollama

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class OllamaAnalysisPayloadTest {

    @Test
    fun `maps a full payload to the domain result`() {
        val payload = OllamaAnalysisPayload(
            objectType = "development_board",
            name = "D1 Mini",
            manufacturer = "LOLIN",
            model = "D1 Mini V4",
            visibleText = listOf("D1 mini", "ESP8266MOD"),
            characteristics = listOf("ESP8266 development board", "USB connector"),
            confidence = 0.85,
            searchQueries = listOf("LOLIN D1 Mini", "Wemos D1 Mini ESP8266"),
        )

        val result = payload.toDomain()

        assertEquals("development_board", result.objectType)
        assertEquals("D1 Mini", result.name)
        assertEquals("LOLIN", result.manufacturer)
        assertEquals("D1 Mini V4", result.model)
        assertEquals(listOf("D1 mini", "ESP8266MOD"), result.visibleText)
        assertEquals(listOf("ESP8266 development board", "USB connector"), result.characteristics)
        assertEquals(0.85, result.confidence)
        assertEquals(listOf("LOLIN D1 Mini", "Wemos D1 Mini ESP8266"), result.searchQueries)
    }

    @Test
    fun `every field can be absent`() {
        val result = OllamaAnalysisPayload().toDomain()

        assertNull(result.objectType)
        assertNull(result.name)
        assertNull(result.manufacturer)
        assertNull(result.model)
        assertNull(result.confidence)
        assertEquals(emptyList<String>(), result.visibleText)
        assertEquals(emptyList<String>(), result.characteristics)
        assertEquals(emptyList<String>(), result.searchQueries)
    }

    @Test
    fun `blank strings are dropped rather than kept as empty values`() {
        val payload = OllamaAnalysisPayload(objectType = "   ", name = "", visibleText = listOf("  ", "real text", ""))

        val result = payload.toDomain()

        assertNull(result.objectType)
        assertNull(result.name)
        assertEquals(listOf("real text"), result.visibleText)
    }

    @Test
    fun `an out-of-range confidence is clamped rather than rejected`() {
        assertEquals(1.0, OllamaAnalysisPayload(confidence = 1.4).toDomain().confidence)
        assertEquals(0.0, OllamaAnalysisPayload(confidence = -0.2).toDomain().confidence)
    }
}
