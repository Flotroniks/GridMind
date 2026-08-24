package org.gridmind.backend.admin.application

/** Reports which models an Ollama server currently has available — used only for the
 * admin status dashboard, never on the image-analysis path itself (see
 * `imageanalysis/application/ImageAnalysisPort.kt` for that). */
interface OllamaDiagnosticsPort {
    /** Throws if the server can't be reached at all; returns an empty list (not an
     * error) if it answers but has no models pulled. */
    fun listAvailableModels(): List<String>
}
