package org.gridmind.backend.catalog.infrastructure.provider.digikey

import org.gridmind.backend.catalog.domain.CatalogImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DigiKeyProductCatalogProviderTest {

    @Test
    fun `maps a full product to a CatalogResult`() {
        val product = DigiKeyProduct(
            manufacturerProductNumber = "ESP32-S3-DEVKITC-1-N16R8",
            description = DigiKeyDescription(
                productDescription = "ESP32-S3 DevKitC-1 N16R8",
                detailedDescription = "ESP32-S3 development board with 16MB flash and 8MB PSRAM.",
            ),
            manufacturer = DigiKeyManufacturer(name = "Espressif Systems"),
            category = DigiKeyCategory(name = "Development Boards"),
            datasheetUrl = "https://example.com/esp32-s3.pdf",
            photoUrl = "https://example.com/esp32-s3.jpg",
        )

        val result = product.toCatalogResult("DigiKey")

        assertEquals("ESP32-S3 DevKitC-1 N16R8", result?.name)
        assertEquals("Espressif Systems", result?.manufacturer)
        assertEquals("ESP32-S3-DEVKITC-1-N16R8", result?.mpn)
        assertEquals("ESP32-S3 development board with 16MB flash and 8MB PSRAM.", result?.description)
        assertEquals("Development Boards", result?.category)
        assertEquals("https://example.com/esp32-s3.pdf", result?.datasheetUrl)
        assertEquals(listOf(CatalogImage(url = "https://example.com/esp32-s3.jpg", provider = "DigiKey")), result?.images)
        assertEquals(listOf("DigiKey"), result?.sources)
    }

    @Test
    fun `returns null when the MPN is missing`() {
        val product = DigiKeyProduct(manufacturerProductNumber = null)

        assertNull(product.toCatalogResult("DigiKey"))
    }

    @Test
    fun `returns null when the MPN is blank`() {
        val product = DigiKeyProduct(manufacturerProductNumber = "   ")

        assertNull(product.toCatalogResult("DigiKey"))
    }

    @Test
    fun `falls back to the MPN as the name when no description is present`() {
        val product = DigiKeyProduct(manufacturerProductNumber = "BME280", description = null)

        assertEquals("BME280", product.toCatalogResult("DigiKey")?.name)
    }

    @Test
    fun `falls back to the short description when no detailed description is present`() {
        val product = DigiKeyProduct(
            manufacturerProductNumber = "BME280",
            description = DigiKeyDescription(productDescription = "Humidity/pressure/temp sensor", detailedDescription = null),
        )

        assertEquals("Humidity/pressure/temp sensor", product.toCatalogResult("DigiKey")?.description)
    }

    @Test
    fun `has no images when no photo url is present`() {
        val product = DigiKeyProduct(manufacturerProductNumber = "BME280", photoUrl = null)

        assertEquals(emptyList<CatalogImage>(), product.toCatalogResult("DigiKey")?.images)
    }
}
