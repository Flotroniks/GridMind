package org.gridmind.backend.imageanalysis.domain

/**
 * A local vision model's best-effort identification of a photographed object. Every
 * field is optional and may be absent: the model must not invent a precise reference it
 * isn't actually confident about, and GridMind must be able to represent "unsure" as
 * absence rather than as a fabricated string.
 */
data class ImageAnalysisResult(
    val objectType: String? = null,
    val name: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val visibleText: List<String> = emptyList(),
    val characteristics: List<String> = emptyList(),
    val confidence: Double? = null,
    val searchQueries: List<String> = emptyList(),
) {
    init {
        confidence?.let {
            require(it in 0.0..1.0) { "Confidence must be between 0.0 and 1.0." }
        }
    }
}
