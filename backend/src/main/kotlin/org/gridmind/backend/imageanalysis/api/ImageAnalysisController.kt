package org.gridmind.backend.imageanalysis.api

import org.gridmind.backend.imageanalysis.application.ImageAnalysisService
import org.gridmind.backend.imageanalysis.domain.ImageAnalysisResult
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Upload → local analysis → structured result. Nothing here is persisted: the image is
 * read into memory for the duration of this request and discarded once the response is
 * sent — no temp file, no call into inventory/media storage.
 */
@RestController
@RequestMapping("/api/image-analysis")
class ImageAnalysisController(
    private val imageAnalysisService: ImageAnalysisService,
) {
    @PostMapping("/analyze", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun analyze(@RequestParam("image") image: MultipartFile): ImageAnalysisResponse {
        val result = imageAnalysisService.analyze(image.bytes)
        return ImageAnalysisResponse.from(result)
    }
}

data class ImageAnalysisResponse(
    val objectType: String?,
    val name: String?,
    val manufacturer: String?,
    val model: String?,
    val visibleText: List<String>,
    val characteristics: List<String>,
    val confidence: Double?,
    val searchQueries: List<String>,
) {
    companion object {
        fun from(result: ImageAnalysisResult): ImageAnalysisResponse = ImageAnalysisResponse(
            objectType = result.objectType,
            name = result.name,
            manufacturer = result.manufacturer,
            model = result.model,
            visibleText = result.visibleText,
            characteristics = result.characteristics,
            confidence = result.confidence,
            searchQueries = result.searchQueries,
        )
    }
}
