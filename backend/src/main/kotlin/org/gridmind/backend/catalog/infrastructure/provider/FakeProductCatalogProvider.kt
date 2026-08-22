package org.gridmind.backend.catalog.infrastructure.provider

import org.gridmind.backend.catalog.domain.CatalogImage
import org.gridmind.backend.catalog.domain.CatalogResult

/**
 * Hardcoded results for local development and tests, before a real provider (DigiKey,
 * Mouser, ...) is wired up. Deliberately not a Spring `@Component` — instantiate it
 * directly where needed instead, so it can never end up collected alongside real
 * providers via `List<ProductCatalogProvider>` injection in production.
 */
class FakeProductCatalogProvider(override val name: String = "Fake") : ProductCatalogProvider {

    override fun search(query: String): List<CatalogResult> {
        if (query.isBlank()) return emptyList()
        return catalog.filter { result ->
            result.name.contains(query, ignoreCase = true) ||
                result.mpn.contains(query, ignoreCase = true) ||
                result.manufacturer?.contains(query, ignoreCase = true) == true
        }
    }

    companion object {
        private val catalog = listOf(
            CatalogResult(
                name = "ESP32-S3 DevKitC-1 N16R8",
                manufacturer = "Espressif",
                mpn = "ESP32-S3-DEVKITC-1-N16R8",
                description = "ESP32-S3 development board with 16MB flash and 8MB PSRAM.",
                category = "Development Boards",
                datasheetUrl = "https://www.espressif.com/sites/default/files/documentation/" +
                    "esp32-s3-devkitc-1_v1.1_specification_en.pdf",
                images = listOf(CatalogImage(url = "https://example.com/esp32-s3.jpg", provider = "Fake")),
                sources = listOf("Fake"),
            ),
            CatalogResult(
                name = "STM32F103C8T6",
                manufacturer = "STMicroelectronics",
                mpn = "STM32F103C8T6",
                description = "ARM Cortex-M3 32-bit microcontroller, 64KB flash.",
                category = "Microcontrollers",
                datasheetUrl = "https://www.st.com/resource/en/datasheet/stm32f103c8.pdf",
                images = listOf(CatalogImage(url = "https://example.com/stm32f103.jpg", provider = "Fake")),
                sources = listOf("Fake"),
            ),
            CatalogResult(
                name = "BME280",
                manufacturer = "Bosch Sensortec",
                mpn = "BME280",
                description = "Combined humidity, pressure and temperature sensor.",
                category = "Sensors",
                datasheetUrl = "https://www.bosch-sensortec.com/media/boschsensortec/downloads/" +
                    "datasheets/bst-bme280-ds002.pdf",
                images = listOf(CatalogImage(url = "https://example.com/bme280.jpg", provider = "Fake")),
                sources = listOf("Fake"),
            ),
        )
    }
}
