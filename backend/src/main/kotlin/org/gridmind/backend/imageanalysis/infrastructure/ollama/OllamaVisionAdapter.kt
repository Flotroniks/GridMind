package org.gridmind.backend.imageanalysis.infrastructure.ollama

import org.gridmind.backend.imageanalysis.application.ImageAnalysisPort
import org.gridmind.backend.imageanalysis.domain.ImageAnalysisResult
import org.gridmind.backend.shared.error.ImageAnalysisUnavailableException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.convert.DurationStyle
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import tools.jackson.databind.ObjectMapper
import java.net.SocketTimeoutException
import java.util.Base64

/**
 * Implements [ImageAnalysisPort] against a local Ollama server. This is the only place in
 * the feature that knows Ollama exists: HTTP request shaping lives in [OllamaApiClient],
 * response mapping lives in [OllamaAnalysisPayload.toDomain], and this class's own job is
 * just to call one, translate its failures into a single clean exception, and hand the
 * other the raw text to parse.
 *
 * No credential gate (unlike the catalog providers): Ollama needs no API key, so this
 * adapter is always registered. What it *does* need is a reachable Ollama instance with
 * the configured model pulled — see the README's "Local AI / Image analysis" section.
 */
@Component
class OllamaVisionAdapter(
    @Value("\${gridmind.imageanalysis.ollama.base-url}") baseUrl: String,
    @Value("\${gridmind.imageanalysis.ollama.model}") private val model: String,
    @Value("\${gridmind.imageanalysis.ollama.timeout}") rawTimeout: String,
    private val objectMapper: ObjectMapper,
) : ImageAnalysisPort {

    private val timeout = DurationStyle.detectAndParse(rawTimeout)
    private val client = OllamaApiClient(baseUrl, model, timeout)

    override fun analyze(imageBytes: ByteArray): ImageAnalysisResult {
        val response = callOllama(imageBytes)
        val content = response.message?.content?.takeIf { it.isNotBlank() }
            ?: throw ImageAnalysisUnavailableException("Ollama returned no analysis.")

        return parse(content)
    }

    private fun callOllama(imageBytes: ByteArray): OllamaChatResponse {
        val imageBase64 = Base64.getEncoder().encodeToString(imageBytes)
        return try {
            client.chatWithImage(imageBase64)
        } catch (e: HttpClientErrorException.NotFound) {
            throw ImageAnalysisUnavailableException("The vision model '$model' is not available on the Ollama server.")
        } catch (e: HttpStatusCodeException) {
            throw ImageAnalysisUnavailableException(
                "The image analysis service returned an error (status ${e.statusCode.value()}).",
            )
        } catch (e: RestClientException) {
            // Covers both a refused/unreachable connection and a timeout — Spring wraps a
            // SocketTimeoutException differently depending on which phase of the request
            // it interrupts (connecting, reading the status line, reading the body), so
            // the cause chain is searched instead of matching one specific wrapper type.
            if (e.hasCause<SocketTimeoutException>()) {
                throw ImageAnalysisUnavailableException(
                    "The image analysis timed out after ${timeout.seconds}s. Try again, or use a simpler image.",
                )
            }
            throw ImageAnalysisUnavailableException("The image analysis service (Ollama) is currently unreachable.")
        }
    }

    private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (current is T) return true
            current = current.cause
        }
        return false
    }

    private fun parse(content: String): ImageAnalysisResult {
        val payload = try {
            objectMapper.readValue(content, OllamaAnalysisPayload::class.java)
        } catch (e: RuntimeException) {
            throw ImageAnalysisUnavailableException("The model's response could not be parsed.")
        }

        return payload.toDomain()
    }
}
