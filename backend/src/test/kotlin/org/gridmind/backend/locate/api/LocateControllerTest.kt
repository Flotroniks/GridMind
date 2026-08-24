package org.gridmind.backend.locate.api

import org.gridmind.backend.locate.application.LocateService
import org.gridmind.backend.shared.config.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(LocateController::class)
@Import(SecurityConfig::class)
class LocateControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var locateService: LocateService

    @Test
    fun `locate returns 204 and delegates the query to the service`() {
        mockMvc.perform(post("/api/locate").param("query", "esp32"))
            .andExpect(status().isNoContent)

        verify(locateService).locate("esp32")
    }

    @Test
    fun `locate accepts a missing query`() {
        mockMvc.perform(post("/api/locate"))
            .andExpect(status().isNoContent)

        verify(locateService).locate(null)
    }
}
