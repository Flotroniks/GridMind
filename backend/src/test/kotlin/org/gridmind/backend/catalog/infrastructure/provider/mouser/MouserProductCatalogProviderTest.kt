package org.gridmind.backend.catalog.infrastructure.provider.mouser

import org.gridmind.backend.catalog.domain.CatalogImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MouserProductCatalogProviderTest {

    @Test
    fun `maps a full part to a CatalogResult`() {
        val part = MouserPart(
            manufacturerPartNumber = "STM32F103C8T6",
            description = "IC MCU 32BIT 64KB FLASH 48LQFP",
            manufacturer = "STMicroelectronics",
            category = "Integrated Circuits (ICs)",
            dataSheetUrl = "https://example.com/stm32f103c8t6.pdf",
            imagePath = "https://example.com/stm32f103c8t6.jpg",
        )

        val result = part.toCatalogResult("Mouser")

        assertEquals("IC MCU 32BIT 64KB FLASH 48LQFP", result?.name)
        assertEquals("STMicroelectronics", result?.manufacturer)
        assertEquals("STM32F103C8T6", result?.mpn)
        assertEquals("IC MCU 32BIT 64KB FLASH 48LQFP", result?.description)
        assertEquals("Integrated Circuits (ICs)", result?.category)
        assertEquals("https://example.com/stm32f103c8t6.pdf", result?.datasheetUrl)
        assertEquals(
            listOf(CatalogImage(url = "https://example.com/stm32f103c8t6.jpg", provider = "Mouser")),
            result?.images,
        )
        assertEquals(listOf("Mouser"), result?.sources)
    }

    @Test
    fun `returns null when the MPN is missing`() {
        val part = MouserPart(manufacturerPartNumber = null)

        assertNull(part.toCatalogResult("Mouser"))
    }

    @Test
    fun `returns null when the MPN is blank`() {
        val part = MouserPart(manufacturerPartNumber = "   ")

        assertNull(part.toCatalogResult("Mouser"))
    }

    @Test
    fun `falls back to the MPN as the name when no description is present`() {
        val part = MouserPart(manufacturerPartNumber = "BME280", description = null)

        assertEquals("BME280", part.toCatalogResult("Mouser")?.name)
    }

    @Test
    fun `has no images when no image path is present`() {
        val part = MouserPart(manufacturerPartNumber = "BME280", imagePath = null)

        assertEquals(emptyList<CatalogImage>(), part.toCatalogResult("Mouser")?.images)
    }
}
