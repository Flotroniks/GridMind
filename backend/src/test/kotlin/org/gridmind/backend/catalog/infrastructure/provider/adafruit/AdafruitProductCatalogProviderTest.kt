package org.gridmind.backend.catalog.infrastructure.provider.adafruit

import org.gridmind.backend.catalog.domain.CatalogImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdafruitProductCatalogProviderTest {

    @Test
    fun `maps a full product to a CatalogResult`() {
        val product = AdafruitProduct(
            productName = "Adafruit Qualia ESP32-S3 for TTL RGB-666 Displays",
            productModel = "Rev B",
            productMpn = "ADA5800",
            productManufacturer = "Adafruit",
            productImage = "https://cdn-shop.adafruit.com/640x480/5800-06.jpg",
        )

        val result = product.toCatalogResult("Adafruit")

        assertEquals("Adafruit Qualia ESP32-S3 for TTL RGB-666 Displays", result?.name)
        assertEquals("Adafruit", result?.manufacturer)
        assertEquals("ADA5800", result?.mpn)
        assertEquals("Rev B", result?.description)
        assertNull(result?.category)
        assertNull(result?.datasheetUrl)
        assertEquals(
            listOf(CatalogImage(url = "https://cdn-shop.adafruit.com/640x480/5800-06.jpg", provider = "Adafruit")),
            result?.images,
        )
        assertEquals(listOf("Adafruit"), result?.sources)
    }

    @Test
    fun `returns null when the product name is missing`() {
        val product = AdafruitProduct(productName = null)

        assertNull(product.toCatalogResult("Adafruit"))
    }

    @Test
    fun `returns null when the product name is blank`() {
        val product = AdafruitProduct(productName = "   ")

        assertNull(product.toCatalogResult("Adafruit"))
    }

    @Test
    fun `mpn is optional, for a maker product without a store SKU in the feed`() {
        val product = AdafruitProduct(productName = "Some board", productMpn = null)

        assertNull(product.toCatalogResult("Adafruit")?.mpn)
    }

    @Test
    fun `falls back to Adafruit as the manufacturer when the feed leaves it null`() {
        val product = AdafruitProduct(productName = "Some board", productManufacturer = null)

        assertEquals("Adafruit", product.toCatalogResult("Adafruit")?.manufacturer)
    }

    @Test
    fun `has no images when no image is present`() {
        val product = AdafruitProduct(productName = "Some board", productImage = null)

        assertEquals(emptyList<CatalogImage>(), product.toCatalogResult("Adafruit")?.images)
    }

    @Test
    fun `discontinued and pending products are not catalogable`() {
        assertFalse(AdafruitProduct(productName = "Old kit", discontinueStatus = "Discontinued").isCatalogable())
        assertFalse(AdafruitProduct(productName = "Upcoming kit", discontinueStatus = "Pending").isCatalogable())
        assertTrue(AdafruitProduct(productName = "Active kit", discontinueStatus = "None").isCatalogable())
    }

    @Test
    fun `virtual (non-physical) products are not catalogable`() {
        assertFalse(AdafruitProduct(productName = "A course", discontinueStatus = "None", productsVirtual = "1").isCatalogable())
        assertTrue(AdafruitProduct(productName = "A board", discontinueStatus = "None", productsVirtual = "0").isCatalogable())
    }

    @Test
    fun `matches name, mpn or model case-insensitively`() {
        val product = AdafruitProduct(productName = "Feather ESP32-S3", productMpn = "ADA5477", productModel = "Rev C")

        assertTrue(product.matches("esp32"))
        assertTrue(product.matches("ADA5477"))
        assertTrue(product.matches("rev c"))
        assertFalse(product.matches("stm32"))
    }
}
