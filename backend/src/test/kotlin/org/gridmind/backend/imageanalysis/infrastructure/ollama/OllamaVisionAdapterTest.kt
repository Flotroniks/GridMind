package org.gridmind.backend.imageanalysis.infrastructure.ollama

import com.sun.net.httpserver.HttpServer
import org.gridmind.backend.shared.error.ImageAnalysisUnavailableException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.databind.json.JsonMapper
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

/**
 * Exercises [OllamaVisionAdapter] against a real (but fake) HTTP server — a plain JDK
 * `com.sun.net.httpserver.HttpServer`, not a real Ollama instance, so this suite never
 * needs Ollama actually running. Covers the failure modes `ProductSearchService`-style
 * providers don't need to (they degrade silently); this feature is a single synchronous
 * request, so every failure mode must turn into one clear, typed exception.
 */
class OllamaVisionAdapterTest {

    private var server: HttpServer? = null
    private val objectMapper = JsonMapper.builder().build()

    @AfterEach
    fun stopServer() {
        server?.stop(0)
    }

    @Test
    fun `maps a valid structured response into an ImageAnalysisResult`() {
        val adapter = adapterWithResponse(
            200,
            chatResponseBody(
                """{"objectType":"development_board","name":"D1 Mini","confidence":0.85,""" +
                    """"visibleText":["D1 mini","ESP8266MOD"]}""",
            ),
        )

        val result = adapter.analyze(byteArrayOf(1, 2, 3))

        assertEquals("development_board", result.objectType)
        assertEquals("D1 Mini", result.name)
        assertEquals(0.85, result.confidence)
        assertEquals(listOf("D1 mini", "ESP8266MOD"), result.visibleText)
    }

    @Test
    fun `omits fields the model left out of the JSON`() {
        val adapter = adapterWithResponse(200, chatResponseBody("""{"name":"Something"}"""))

        val result = adapter.analyze(byteArrayOf(1))

        assertEquals("Something", result.name)
        assertEquals(null, result.manufacturer)
        assertEquals(emptyList<String>(), result.characteristics)
    }

    @Test
    fun `throws when Ollama is unreachable`() {
        // Bind then immediately close, so nothing is listening on this port — a
        // deterministic "connection refused" on any platform, unlike a fixed low port
        // number which some environments silently drop instead of refusing.
        val closedPort = java.net.ServerSocket(0).use { it.localPort }
        val adapter = OllamaVisionAdapter(
            baseUrl = "http://localhost:$closedPort",
            model = "qwen2.5vl:3b",
            rawTimeout = "2s",
            objectMapper = objectMapper,
        )

        assertThrows<ImageAnalysisUnavailableException> { adapter.analyze(byteArrayOf(1)) }
    }

    @Test
    fun `throws a clear error when the model is not found`() {
        val adapter = adapterWithResponse(404, """{"error":"model 'qwen2.5vl:3b' not found"}""")

        val exception = assertThrows<ImageAnalysisUnavailableException> { adapter.analyze(byteArrayOf(1)) }

        assertTrue(exception.message!!.contains("not available"), exception.message)
    }

    @Test
    fun `throws a clear error on an Ollama internal error`() {
        val adapter = adapterWithResponse(500, """{"error":"internal error"}""")

        assertThrows<ImageAnalysisUnavailableException> { adapter.analyze(byteArrayOf(1)) }
    }

    @Test
    fun `throws when the response has no message content`() {
        val adapter = adapterWithResponse(200, """{"message":{"role":"assistant","content":""},"done":true}""")

        val exception = assertThrows<ImageAnalysisUnavailableException> { adapter.analyze(byteArrayOf(1)) }

        assertEquals("Ollama returned no analysis.", exception.message)
    }

    @Test
    fun `throws when the response body is empty`() {
        val adapter = adapterWithResponse(200, "")

        assertThrows<ImageAnalysisUnavailableException> { adapter.analyze(byteArrayOf(1)) }
    }

    @Test
    fun `throws when the message content isn't valid JSON`() {
        val adapter = adapterWithResponse(200, chatResponseBody("not json at all"))

        val exception = assertThrows<ImageAnalysisUnavailableException> { adapter.analyze(byteArrayOf(1)) }

        assertEquals("The model's response could not be parsed.", exception.message)
    }

    @Test
    fun `throws a timeout error when Ollama takes too long to respond`() {
        val adapter = adapterThatDelays(millis = 800, timeout = "200ms")

        val exception = assertThrows<ImageAnalysisUnavailableException> { adapter.analyze(byteArrayOf(1)) }

        assertTrue(exception.message!!.contains("timed out"), exception.message)
    }

    /** `content` is itself a JSON string embedded inside the outer chat response JSON. */
    private fun chatResponseBody(content: String): String {
        val escaped = objectMapper.writeValueAsString(content)
        return """{"message":{"role":"assistant","content":$escaped},"done":true}"""
    }

    private fun adapterWithResponse(status: Int, body: String): OllamaVisionAdapter {
        val httpServer = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        httpServer.createContext("/api/chat") { exchange ->
            val bytes = body.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json")
            // -1 signals "no body" to HttpServer; passing 0 here would switch it to
            // chunked transfer encoding instead of an actual empty, Content-Length: 0 body.
            exchange.sendResponseHeaders(status, if (bytes.isEmpty()) -1L else bytes.size.toLong())
            exchange.responseBody.use { if (bytes.isNotEmpty()) it.write(bytes) }
        }
        httpServer.start()
        server = httpServer

        return OllamaVisionAdapter(
            baseUrl = "http://localhost:${httpServer.address.port}",
            model = "qwen2.5vl:3b",
            rawTimeout = "5s",
            objectMapper = objectMapper,
        )
    }

    private fun adapterThatDelays(millis: Long, timeout: String): OllamaVisionAdapter {
        val httpServer = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        httpServer.createContext("/api/chat") { exchange ->
            Thread.sleep(millis)
            val bytes = chatResponseBody("""{"name":"Too slow"}""").toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        httpServer.start()
        server = httpServer

        return OllamaVisionAdapter(
            baseUrl = "http://localhost:${httpServer.address.port}",
            model = "qwen2.5vl:3b",
            rawTimeout = timeout,
            objectMapper = objectMapper,
        )
    }
}
