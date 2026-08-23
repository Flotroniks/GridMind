package org.gridmind.backend.catalog.infrastructure.provider.ebay

import org.gridmind.backend.catalog.domain.CatalogImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EbayProductCatalogProviderTest {

    @Test
    fun `maps a full item to a CatalogResult`() {
        val item = EbayItemSummary(
            title = "STM32F103C8T6 Blue Pill Development Board",
            shortDescription = "STM32F103C8T6 ARM Cortex-M3 development board, new, unused.",
            condition = "New",
            price = EbayConvertedAmount(value = "3.99", currency = "USD"),
            image = EbayImage(imageUrl = "https://example.com/bluepill.jpg"),
            categories = listOf(
                EbayCategory(categoryId = "1", categoryName = "Electronics"),
                EbayCategory(categoryId = "2", categoryName = "Development Boards"),
            ),
            leafCategoryIds = listOf("2"),
        )

        val result = item.toCatalogResult("eBay")

        assertEquals("STM32F103C8T6 Blue Pill Development Board", result?.name)
        assertNull(result?.manufacturer)
        assertNull(result?.mpn)
        assertEquals("STM32F103C8T6 ARM Cortex-M3 development board, new, unused.", result?.description)
        assertEquals("Development Boards", result?.category)
        assertNull(result?.datasheetUrl)
        assertEquals(
            listOf(CatalogImage(url = "https://example.com/bluepill.jpg", provider = "eBay")),
            result?.images,
        )
        assertEquals(listOf("eBay"), result?.sources)
    }

    @Test
    fun `returns null when the title is missing`() {
        val item = EbayItemSummary(title = null)

        assertNull(item.toCatalogResult("eBay"))
    }

    @Test
    fun `returns null when the title is blank`() {
        val item = EbayItemSummary(title = "   ")

        assertNull(item.toCatalogResult("eBay"))
    }

    @Test
    fun `falls back to condition and price when no short description is present`() {
        val item = EbayItemSummary(
            title = "BME280 breakout",
            shortDescription = null,
            condition = "Used",
            price = EbayConvertedAmount(value = "5.50", currency = "EUR"),
        )

        assertEquals("Used — 5.50 EUR", item.toCatalogResult("eBay")?.description)
    }

    @Test
    fun `falls back to just the condition when there is no price`() {
        val item = EbayItemSummary(title = "BME280 breakout", shortDescription = null, condition = "Used", price = null)

        assertEquals("Used", item.toCatalogResult("eBay")?.description)
    }

    @Test
    fun `has no description when neither short description, condition nor price are present`() {
        val item = EbayItemSummary(title = "BME280 breakout", shortDescription = null, condition = null, price = null)

        assertNull(item.toCatalogResult("eBay")?.description)
    }

    @Test
    fun `resolves the category by matching leafCategoryIds rather than trusting array order`() {
        val item = EbayItemSummary(
            title = "BME280 breakout",
            categories = listOf(
                EbayCategory(categoryId = "1", categoryName = "Electronics"),
                EbayCategory(categoryId = "2", categoryName = "Sensors"),
            ),
            leafCategoryIds = listOf("1"),
        )

        assertEquals("Electronics", item.toCatalogResult("eBay")?.category)
    }

    @Test
    fun `falls back to the last category when no leaf match is found`() {
        val item = EbayItemSummary(
            title = "BME280 breakout",
            categories = listOf(
                EbayCategory(categoryId = "1", categoryName = "Electronics"),
                EbayCategory(categoryId = "2", categoryName = "Sensors"),
            ),
            leafCategoryIds = null,
        )

        assertEquals("Sensors", item.toCatalogResult("eBay")?.category)
    }

    @Test
    fun `has no images when no image is present`() {
        val item = EbayItemSummary(title = "BME280 breakout", image = null)

        assertEquals(emptyList<CatalogImage>(), item.toCatalogResult("eBay")?.images)
    }
}
