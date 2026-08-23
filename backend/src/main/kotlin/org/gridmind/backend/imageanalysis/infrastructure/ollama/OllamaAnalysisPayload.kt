package org.gridmind.backend.imageanalysis.infrastructure.ollama

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.gridmind.backend.imageanalysis.domain.ImageAnalysisResult

/**
 * Mirrors the JSON schema handed to Ollama in [OllamaApiClient.RESPONSE_SCHEMA] — the
 * shape `message.content` (a JSON string) is parsed into. Every field is nullable: Ollama
 * enforces the *shape*, not that a given field is filled in.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OllamaAnalysisPayload(
    @JsonProperty("objectType") val objectType: String? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("manufacturer") val manufacturer: String? = null,
    @JsonProperty("model") val model: String? = null,
    @JsonProperty("visibleText") val visibleText: List<String>? = null,
    @JsonProperty("characteristics") val characteristics: List<String>? = null,
    @JsonProperty("confidence") val confidence: Double? = null,
    @JsonProperty("searchQueries") val searchQueries: List<String>? = null,
)

/**
 * Kept as a standalone function (not a method) so it's testable without a client or a
 * Spring context — same pattern as the catalog providers' `toCatalogResult` functions.
 * Blank strings are dropped and confidence is clamped into [0,1] rather than treated as a
 * schema violation: the model staying in range is normally guaranteed by the JSON schema,
 * but a very slight drift shouldn't fail the whole analysis over one field.
 */
internal fun OllamaAnalysisPayload.toDomain(): ImageAnalysisResult = ImageAnalysisResult(
    objectType = objectType?.trim()?.takeIf { it.isNotEmpty() },
    name = name?.trim()?.takeIf { it.isNotEmpty() },
    manufacturer = manufacturer?.trim()?.takeIf { it.isNotEmpty() },
    model = model?.trim()?.takeIf { it.isNotEmpty() },
    visibleText = visibleText.orEmpty().mapNotNull { it.trim().takeIf(String::isNotEmpty) },
    characteristics = characteristics.orEmpty().mapNotNull { it.trim().takeIf(String::isNotEmpty) },
    confidence = confidence?.coerceIn(0.0, 1.0),
    searchQueries = searchQueries.orEmpty().mapNotNull { it.trim().takeIf(String::isNotEmpty) },
)
