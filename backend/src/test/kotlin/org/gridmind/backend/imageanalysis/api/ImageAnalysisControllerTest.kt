package org.gridmind.backend.imageanalysis.api

import org.gridmind.backend.imageanalysis.application.ImageAnalysisService
import org.gridmind.backend.imageanalysis.domain.ImageAnalysisResult
import org.gridmind.backend.shared.config.SecurityConfig
import org.gridmind.backend.shared.error.ImageAnalysisUnavailableException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ImageAnalysisController::class)
@Import(SecurityConfig::class)
class ImageAnalysisControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var imageAnalysisService: ImageAnalysisService

    @Test
    fun `analyze returns 200 with the mapped result for a valid upload`() {
        val file = MockMultipartFile("image", "board.jpg", "image/jpeg", byteArrayOf(1, 2, 3))
        `when`(imageAnalysisService.analyze(file.bytes)).thenReturn(
            ImageAnalysisResult(
                objectType = "development_board",
                name = "D1 Mini",
                confidence = 0.85,
                visibleText = listOf("D1 mini", "ESP8266MOD"),
            ),
        )

        mockMvc.perform(multipart("/api/image-analysis/analyze").file(file))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.objectType").value("development_board"))
            .andExpect(jsonPath("$.name").value("D1 Mini"))
            .andExpect(jsonPath("$.confidence").value(0.85))
            .andExpect(jsonPath("$.visibleText[0]").value("D1 mini"))
    }

    @Test
    fun `analyze returns 400 when the service rejects the file`() {
        val file = MockMultipartFile("image", "empty.jpg", "image/jpeg", ByteArray(0))
        `when`(imageAnalysisService.analyze(file.bytes)).thenThrow(IllegalArgumentException("The uploaded file is empty."))

        mockMvc.perform(multipart("/api/image-analysis/analyze").file(file))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("The uploaded file is empty."))
    }

    @Test
    fun `analyze returns 503 when the analysis backend is unavailable`() {
        val file = MockMultipartFile("image", "board.jpg", "image/jpeg", byteArrayOf(1, 2, 3))
        `when`(imageAnalysisService.analyze(file.bytes))
            .thenThrow(ImageAnalysisUnavailableException("The image analysis service (Ollama) is currently unreachable."))

        mockMvc.perform(multipart("/api/image-analysis/analyze").file(file))
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.message").value("The image analysis service (Ollama) is currently unreachable."))
    }

    @Test
    fun `analyze returns 400 when no file part is provided`() {
        mockMvc.perform(multipart("/api/image-analysis/analyze"))
            .andExpect(status().isBadRequest)
    }
}
