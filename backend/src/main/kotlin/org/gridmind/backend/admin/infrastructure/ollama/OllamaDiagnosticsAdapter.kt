package org.gridmind.backend.admin.infrastructure.ollama

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.gridmind.backend.admin.application.OllamaDiagnosticsPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Calls Ollama's `/api/tags` endpoint directly — deliberately independent of
 * `ImageAnalysisPort`/`OllamaVisionAdapter`, since this is a diagnostic concern (is the
 * server up, is the model pulled) rather than the image-analysis use case itself. Short
 * timeouts on purpose: this backs a status dashboard a person is actively looking at,
 * not a background job — it should fail fast rather than make the page hang.
 */
@Component
class OllamaDiagnosticsAdapter(
    @Value("\${gridmind.imageanalysis.ollama.base-url}") baseUrl: String,
) : OllamaDiagnosticsPort {
    private val restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .configureMessageConverters { it.withJsonConverter(JacksonJsonHttpMessageConverter()) }
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(3_000)
                setReadTimeout(3_000)
            },
        )
        .build()

    override fun listAvailableModels(): List<String> =
        restClient.get()
            .uri("/api/tags")
            .retrieve()
            .body(OllamaTagsResponse::class.java)
            ?.models
            ?.map { it.name }
            ?: emptyList()
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OllamaTagsResponse(val models: List<OllamaTagModel> = emptyList())

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OllamaTagModel(val name: String = "")
