package org.gridmind.backend.catalog.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductGrouperTest {

    @Test
    fun `merges same manufacturer and mpn from different providers`() {
        val digikey = CatalogResult(
            name = "ESP32-S3 DevKitC-1 N16R8",
            manufacturer = "Espressif",
            mpn = "ESP32-S3-DEVKITC-1-N16R8",
            datasheetUrl = "https://example.com/esp32-s3.pdf",
            images = listOf(CatalogImage(url = "https://example.com/digikey.jpg", provider = "DigiKey")),
            sources = listOf("DigiKey"),
        )
        val mouser = CatalogResult(
            name = "ESP32-S3-DevKitC-1-N16R8",
            manufacturer = "espressif", // same manufacturer, different casing
            mpn = "ESP32-S3-DEVKITC-1-N16R8",
            description = "ESP32-S3 development board.",
            images = listOf(CatalogImage(url = "https://example.com/mouser.jpg", provider = "Mouser")),
            sources = listOf("Mouser"),
        )

        val grouped = ProductGrouper.group(listOf(digikey, mouser))

        assertEquals(1, grouped.size)
        val merged = grouped.single()
        assertEquals("ESP32-S3-DEVKITC-1-N16R8", merged.mpn)
        assertEquals("Espressif", merged.manufacturer) // first non-null wins
        assertEquals("ESP32-S3 development board.", merged.description) // filled in from the second source
        assertEquals("https://example.com/esp32-s3.pdf", merged.datasheetUrl)
        assertEquals(setOf("DigiKey", "Mouser"), merged.sources.toSet())
        assertEquals(2, merged.images.size)
    }

    @Test
    fun `mpn normalization ignores case, spacing and dashes`() {
        val a = CatalogResult(name = "Part A", manufacturer = "Acme", mpn = "ABC-123", sources = listOf("A"))
        val b = CatalogResult(name = "Part A variant", manufacturer = "Acme", mpn = " abc123 ", sources = listOf("B"))

        val grouped = ProductGrouper.group(listOf(a, b))

        assertEquals(1, grouped.size)
        assertEquals(setOf("A", "B"), grouped.single().sources.toSet())
    }

    @Test
    fun `different products are kept separate`() {
        val esp32 = CatalogResult(name = "ESP32-S3", manufacturer = "Espressif", mpn = "ESP32-S3", sources = listOf("DigiKey"))
        val stm32 = CatalogResult(name = "STM32F103C8T6", manufacturer = "STMicroelectronics", mpn = "STM32F103C8T6", sources = listOf("DigiKey"))

        val grouped = ProductGrouper.group(listOf(esp32, stm32))

        assertEquals(2, grouped.size)
    }

    @Test
    fun `same mpn from different manufacturers is not merged`() {
        val genericA = CatalogResult(name = "10k resistor", manufacturer = "Yageo", mpn = "R-10K", sources = listOf("DigiKey"))
        val genericB = CatalogResult(name = "10k resistor", manufacturer = "Vishay", mpn = "R-10K", sources = listOf("Mouser"))

        val grouped = ProductGrouper.group(listOf(genericA, genericB))

        assertEquals(2, grouped.size)
    }

    @Test
    fun `manufacturer name variants are not merged - no fuzzy matching`() {
        val digikey = CatalogResult(name = "ESP32-S3", manufacturer = "Espressif", mpn = "ESP32-S3", sources = listOf("DigiKey"))
        val mouser = CatalogResult(name = "ESP32-S3", manufacturer = "Espressif Systems", mpn = "ESP32-S3", sources = listOf("Mouser"))

        val grouped = ProductGrouper.group(listOf(digikey, mouser))

        assertEquals(2, grouped.size)
    }

    @Test
    fun `result missing manufacturer still merges into a matching mpn group`() {
        val withManufacturer = CatalogResult(name = "BME280", manufacturer = "Bosch", mpn = "BME280", sources = listOf("DigiKey"))
        val withoutManufacturer = CatalogResult(name = "BME280 sensor", manufacturer = null, mpn = "BME280", sources = listOf("PartsDB"))

        val grouped = ProductGrouper.group(listOf(withManufacturer, withoutManufacturer))

        assertEquals(1, grouped.size)
        assertEquals("Bosch", grouped.single().manufacturer)
        assertEquals(setOf("DigiKey", "PartsDB"), grouped.single().sources.toSet())
    }

    @Test
    fun `results without an mpn are never merged, even with the same name and manufacturer`() {
        // Maker boards often don't have a real MPN — with no reliable key to merge on,
        // two results from different providers for what might be the same board are kept
        // as separate entries rather than guessed into one.
        val fromProviderA = CatalogResult(name = "Wemos D1 Mini", manufacturer = "Wemos", mpn = null, sources = listOf("A"))
        val fromProviderB = CatalogResult(name = "Wemos D1 Mini", manufacturer = "Wemos", mpn = null, sources = listOf("B"))

        val grouped = ProductGrouper.group(listOf(fromProviderA, fromProviderB))

        assertEquals(2, grouped.size)
    }

    @Test
    fun `an mpn-less result never absorbs a later result that does have an mpn`() {
        val makerBoard = CatalogResult(name = "Generic ESP32 board", manufacturer = "Acme", mpn = null, sources = listOf("A"))
        val distributorPart = CatalogResult(name = "Generic ESP32 board", manufacturer = "Acme", mpn = "ESP32-GEN", sources = listOf("B"))

        val grouped = ProductGrouper.group(listOf(makerBoard, distributorPart))

        assertEquals(2, grouped.size)
    }
}
