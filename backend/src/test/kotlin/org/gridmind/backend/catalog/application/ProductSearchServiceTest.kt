package org.gridmind.backend.catalog.application

import org.gridmind.backend.catalog.domain.CatalogResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProductSearchServiceTest {

    @Test
    fun `search merges results from every provider`() {
        val providerA = fakeProvider(
            "A",
            listOf(CatalogResult(name = "ESP32-S3", manufacturer = "Espressif", mpn = "ESP32-S3", sources = listOf("A"))),
        )
        val providerB = fakeProvider(
            "B",
            listOf(CatalogResult(name = "ESP32-S3", manufacturer = "Espressif", mpn = "ESP32-S3", sources = listOf("B"))),
        )

        val service = ProductSearchService(listOf(providerA, providerB))

        val results = service.search("esp32")

        assertEquals(1, results.size)
        assertEquals(setOf("A", "B"), results[0].sources.toSet())
    }

    @Test
    fun `search keeps going when a provider throws`() {
        val healthy = fakeProvider(
            "Healthy",
            listOf(CatalogResult(name = "BME280", manufacturer = "Bosch", mpn = "BME280", sources = listOf("Healthy"))),
        )
        val broken = object : ProductCatalogProvider {
            override val name = "Broken"
            override fun search(query: String): List<CatalogResult> = throw RuntimeException("boom")
        }

        val service = ProductSearchService(listOf(healthy, broken))

        val results = service.search("bme")

        assertEquals(1, results.size)
        assertEquals("BME280", results[0].mpn)
    }

    @Test
    fun `search returns an empty list when there are no providers`() {
        val service = ProductSearchService(emptyList())

        assertTrue(service.search("anything").isEmpty())
    }

    @Test
    fun `search returns an empty list when every provider finds nothing`() {
        val empty = fakeProvider("Empty", emptyList())

        val service = ProductSearchService(listOf(empty))

        assertTrue(service.search("nonexistent-part").isEmpty())
    }

    @Test
    fun `searchMany merges results from every query, deduplicating across queries`() {
        val provider = object : ProductCatalogProvider {
            override val name = "Fake"
            override fun search(query: String): List<CatalogResult> = when (query) {
                "ESP32" -> listOf(CatalogResult(name = "ESP32-S3", manufacturer = "Espressif", mpn = "ESP32-S3", sources = listOf("Fake")))
                "TFT display board" -> listOf(
                    CatalogResult(name = "ESP32-S3", manufacturer = "Espressif", mpn = "ESP32-S3", sources = listOf("Fake")),
                    CatalogResult(name = "TFT Display", manufacturer = "Adafruit", mpn = "TFT-1", sources = listOf("Fake")),
                )
                else -> emptyList()
            }
        }

        val service = ProductSearchService(listOf(provider))

        val results = service.searchMany(listOf("ESP32", "TFT display board"))

        assertEquals(2, results.size)
        assertTrue(results.any { it.mpn == "ESP32-S3" })
        assertTrue(results.any { it.mpn == "TFT-1" })
    }

    @Test
    fun `searchMany ignores blank queries and returns an empty list when none remain`() {
        val service = ProductSearchService(listOf(fakeProvider("Fake", emptyList())))

        assertTrue(service.searchMany(listOf("  ", "")).isEmpty())
    }

    private fun fakeProvider(providerName: String, results: List<CatalogResult>): ProductCatalogProvider =
        object : ProductCatalogProvider {
            override val name = providerName
            override fun search(query: String): List<CatalogResult> = results
        }
}
