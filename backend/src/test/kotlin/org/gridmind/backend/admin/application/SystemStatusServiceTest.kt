package org.gridmind.backend.admin.application

import org.gridmind.backend.catalog.application.ProductCatalogProvider
import org.gridmind.backend.catalog.domain.CatalogResult
import org.gridmind.backend.locate.application.LocatePublisherPort
import org.gridmind.backend.locate.domain.LocateHighlight
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SystemStatusServiceTest {

    @Test
    fun `reports a provider configured only when it registered as a bean`() {
        val service = SystemStatusService(
            catalogProviders = listOf(fakeProvider("DigiKey"), fakeProvider("Adafruit")),
            ollamaDiagnosticsPort = FakeOllamaDiagnosticsPort(listOf("qwen2.5vl:7b")),
            locatePublisherPort = FakePublisher(connected = true),
            configuredOllamaModel = "qwen2.5vl:7b",
        )

        val status = service.currentStatus().catalogProviders.associateBy { it.name }

        assertTrue(status.getValue("DigiKey").configured)
        assertTrue(status.getValue("Adafruit").configured)
        assertFalse(status.getValue("Mouser").configured)
        assertFalse(status.getValue("eBay").configured)
    }

    @Test
    fun `reports ollama configured only when the configured model is actually available`() {
        val service = SystemStatusService(
            catalogProviders = emptyList(),
            ollamaDiagnosticsPort = FakeOllamaDiagnosticsPort(listOf("llama3.2-vision:11b")),
            locatePublisherPort = FakePublisher(connected = true),
            configuredOllamaModel = "qwen2.5vl:7b",
        )

        assertFalse(service.currentStatus().ollama.configured)
    }

    @Test
    fun `reports ollama unreachable when the diagnostics port throws`() {
        val service = SystemStatusService(
            catalogProviders = emptyList(),
            ollamaDiagnosticsPort = ThrowingOllamaDiagnosticsPort(),
            locatePublisherPort = FakePublisher(connected = true),
            configuredOllamaModel = "qwen2.5vl:7b",
        )

        val ollama = service.currentStatus().ollama
        assertFalse(ollama.configured)
        assertEquals("Serveur injoignable", ollama.detail)
    }

    @Test
    fun `reports mqtt status from the publisher's own connectivity check`() {
        val service = SystemStatusService(
            catalogProviders = emptyList(),
            ollamaDiagnosticsPort = FakeOllamaDiagnosticsPort(emptyList()),
            locatePublisherPort = FakePublisher(connected = false),
            configuredOllamaModel = "qwen2.5vl:7b",
        )

        assertFalse(service.currentStatus().mqtt.configured)
    }

    private fun fakeProvider(providerName: String): ProductCatalogProvider =
        object : ProductCatalogProvider {
            override val name = providerName
            override fun search(query: String): List<CatalogResult> = emptyList()
        }

    private class FakeOllamaDiagnosticsPort(private val models: List<String>) : OllamaDiagnosticsPort {
        override fun listAvailableModels(): List<String> = models
    }

    private class ThrowingOllamaDiagnosticsPort : OllamaDiagnosticsPort {
        override fun listAvailableModels(): List<String> = throw RuntimeException("unreachable")
    }

    private class FakePublisher(private val connected: Boolean) : LocatePublisherPort {
        override fun publish(highlights: List<LocateHighlight>) = Unit
        override fun isConnected(): Boolean = connected
    }
}
