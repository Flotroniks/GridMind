package org.gridmind.backend.imageanalysis.infrastructure.ollama

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.RestClient
import java.time.Duration

/**
 * Talks to Ollama's chat endpoint (`/api/chat`) with a single image attached and a JSON
 * schema in `format`, so Ollama itself constrains the model's output to that shape —
 * there's no separate "hope the model returned valid JSON" step. Deliberately dumb: it
 * only knows how to call Ollama and hand back its raw response; mapping to
 * `ImageAnalysisResult` and all error translation live in [OllamaVisionAdapter].
 *
 * Builds its own [RestClient] for the same reason as
 * [org.gridmind.backend.catalog.infrastructure.provider.digikey.DigiKeyApiClient]: this
 * project doesn't pull in a starter that autoconfigures one. The read timeout is the only
 * one that needs to be generous here — CPU inference on a multimodal model is genuinely
 * slow, and that's an accepted trade-off for a prototype (see the README).
 */
class OllamaApiClient(
    baseUrl: String,
    private val model: String,
    timeout: Duration,
) {
    private val restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .configureMessageConverters { it.withJsonConverter(JacksonJsonHttpMessageConverter()) }
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(5_000)
                setReadTimeout(timeout.toMillis().toInt())
            },
        )
        .build()

    fun chatWithImage(imageBase64: String): OllamaChatResponse =
        restClient.post()
            .uri("/api/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                OllamaChatRequest(
                    model = model,
                    messages = listOf(
                        OllamaChatMessage(role = "system", content = VISION_ANALYSIS_PROMPT),
                        OllamaChatMessage(
                            role = "user",
                            content = "Analyse cette image et identifie l'objet représenté.",
                            images = listOf(imageBase64),
                        ),
                    ),
                    format = RESPONSE_SCHEMA,
                    options = GENERATION_OPTIONS,
                ),
            )
            .retrieve()
            .body(OllamaChatResponse::class.java)
            ?: OllamaChatResponse()

    companion object {
        /**
         * A plain Kotlin map, not a hand-built JSON string: Spring's Jackson message
         * converter already serializes whatever object is passed as the request body, so
         * there's no need to touch `ObjectMapper`/`JsonNode` directly just to describe a
         * schema. No field is `required`, matching the domain rule that every field of
         * `ImageAnalysisResult` may legitimately be absent.
         */
        private val RESPONSE_SCHEMA: Map<String, Any> = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "objectType" to mapOf("type" to "string"),
                "name" to mapOf("type" to "string"),
                "manufacturer" to mapOf("type" to "string"),
                "model" to mapOf("type" to "string"),
                "visibleText" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "characteristics" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
                "confidence" to mapOf("type" to "number"),
                "searchQueries" to mapOf("type" to "array", "items" to mapOf("type" to "string")),
            ),
            "required" to emptyList<String>(),
        )

        /**
         * Observed empirically: with no `num_predict` cap, schema-constrained decoding on
         * this model can occasionally fall into a repetition loop and never emit the
         * closing brace, generating thousands of tokens instead of the few hundred the
         * schema actually needs — turning a normal ~10-30s analysis into one that runs for
         * minutes before the read timeout finally cuts it off. `num_predict` bounds the
         * worst case; `repeat_penalty` above Ollama's own default pushes back specifically
         * against that repetition failure mode; a low temperature favors the
         * deterministic, literal reading this identification task wants over creative
         * phrasing. None of this fully eliminates the failure mode on a 3B model — see the
         * README's "Local AI / Image analysis" limitations for what's still observed.
         */
        private val GENERATION_OPTIONS: Map<String, Any> = mapOf(
            "num_predict" to 768,
            "temperature" to 0.2,
            "repeat_penalty" to 1.3,
        )
    }
}

data class OllamaChatRequest(
    @JsonProperty("model") val model: String,
    @JsonProperty("messages") val messages: List<OllamaChatMessage>,
    @JsonProperty("format") val format: Any,
    @JsonProperty("options") val options: Map<String, Any>? = null,
    @JsonProperty("stream") val stream: Boolean = false,
)

data class OllamaChatMessage(
    @JsonProperty("role") val role: String,
    @JsonProperty("content") val content: String,
    @JsonProperty("images") val images: List<String>? = null,
)

/** Only the fields GridMind actually reads — Ollama's real response also carries timing
 * and token-count statistics, deliberately ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OllamaChatResponse(
    @JsonProperty("message") val message: OllamaChatResponseMessage? = null,
    @JsonProperty("done") val done: Boolean = false,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OllamaChatResponseMessage(
    @JsonProperty("role") val role: String? = null,
    @JsonProperty("content") val content: String? = null,
)
