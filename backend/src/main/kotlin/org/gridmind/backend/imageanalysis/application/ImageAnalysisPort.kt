package org.gridmind.backend.imageanalysis.application

import org.gridmind.backend.imageanalysis.domain.ImageAnalysisResult

/**
 * Port to a local multimodal vision model that identifies a photographed object from its
 * raw bytes. The one adapter today
 * ([org.gridmind.backend.imageanalysis.infrastructure.ollama.OllamaVisionAdapter]) talks
 * to Ollama, but nothing above this interface knows that — same pragmatic-hexagonal
 * pattern as [org.gridmind.backend.catalog.application.ProductCatalogProvider] and
 * [org.gridmind.backend.inventory.application.ImageDownloader]: an external dependency
 * sits behind a port owned by the use case that depends on it.
 */
interface ImageAnalysisPort {
    fun analyze(imageBytes: ByteArray): ImageAnalysisResult
}
