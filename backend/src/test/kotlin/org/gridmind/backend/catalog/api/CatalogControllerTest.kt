package org.gridmind.backend.catalog.api

import org.gridmind.backend.catalog.application.ProductSearchService
import org.gridmind.backend.catalog.domain.CatalogImage
import org.gridmind.backend.catalog.domain.CatalogResult
import org.gridmind.backend.shared.config.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CatalogController::class)
@Import(SecurityConfig::class)
class CatalogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var productSearchService: ProductSearchService

    @Test
    fun `search returns 200 with the mapped results`() {
        `when`(productSearchService.search("esp32")).thenReturn(
            listOf(
                CatalogResult(
                    name = "ESP32-S3 DevKitC-1 N16R8",
                    manufacturer = "Espressif",
                    mpn = "ESP32-S3-DEVKITC-1-N16R8",
                    images = listOf(CatalogImage(url = "https://example.com/esp32.jpg", provider = "DigiKey")),
                    sources = listOf("DigiKey"),
                ),
            ),
        )

        mockMvc.perform(get("/api/catalog/search").param("query", "esp32"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("ESP32-S3 DevKitC-1 N16R8"))
            .andExpect(jsonPath("$[0].mpn").value("ESP32-S3-DEVKITC-1-N16R8"))
            .andExpect(jsonPath("$[0].images[0].url").value("https://example.com/esp32.jpg"))
            .andExpect(jsonPath("$[0].sources[0]").value("DigiKey"))
    }

    @Test
    fun `search returns 400 for a blank query`() {
        mockMvc.perform(get("/api/catalog/search").param("query", "   "))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `search returns results without an mpn, for maker hardware without a real part number`() {
        `when`(productSearchService.search("d1 mini")).thenReturn(
            listOf(CatalogResult(name = "Wemos D1 Mini", manufacturer = "Wemos", sources = listOf("Fake"))),
        )

        mockMvc.perform(get("/api/catalog/search").param("query", "d1 mini"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Wemos D1 Mini"))
            .andExpect(jsonPath("$[0].mpn").doesNotExist())
    }

    @Test
    fun `searchMany returns 200 with the mapped results`() {
        `when`(productSearchService.searchMany(listOf("ESP32", "TFT display"))).thenReturn(
            listOf(CatalogResult(name = "ESP32-S3", manufacturer = "Espressif", mpn = "ESP32-S3", sources = listOf("Fake"))),
        )

        mockMvc.perform(get("/api/catalog/search-many").param("queries", "ESP32", "TFT display"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("ESP32-S3"))
    }

    @Test
    fun `searchMany returns 400 when every query is blank`() {
        mockMvc.perform(get("/api/catalog/search-many").param("queries", "  ", ""))
            .andExpect(status().isBadRequest)
    }
}
