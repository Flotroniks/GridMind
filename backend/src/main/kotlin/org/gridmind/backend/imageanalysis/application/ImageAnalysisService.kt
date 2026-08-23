package org.gridmind.backend.imageanalysis.application

import org.gridmind.backend.imageanalysis.domain.ImageAnalysisResult
import org.gridmind.backend.shared.validation.UploadedImageValidator
import org.springframework.stereotype.Service

/**
 * The image-analysis use case: validate the upload, then hand it to whatever
 * [ImageAnalysisPort] is configured. Nothing here knows or cares that the port happens to
 * be backed by Ollama — see [ImageAnalysisPort].
 */
@Service
class ImageAnalysisService(private val imageAnalysisPort: ImageAnalysisPort) {

    fun analyze(imageBytes: ByteArray): ImageAnalysisResult {
        UploadedImageValidator.validate(imageBytes)
        return imageAnalysisPort.analyze(imageBytes)
    }
}
