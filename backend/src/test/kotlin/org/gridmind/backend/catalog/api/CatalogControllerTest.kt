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
}
