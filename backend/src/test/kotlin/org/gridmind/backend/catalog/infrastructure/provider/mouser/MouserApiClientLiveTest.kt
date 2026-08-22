package org.gridmind.backend.catalog.infrastructure.provider.mouser

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

/**
 * A real call against Mouser's Search API — skipped entirely unless MOUSER_API_KEY is
 * set in the environment, so `./gradlew test` never depends on network access or
 * credentials. Run it deliberately once you've filled in MOUSER_API_KEY (see
 * .env.example) to confirm the real integration still works, e.g.
 * `MOUSER_API_KEY=... ./gradlew test --tests "*MouserApiClientLiveTest*"`.
 */
@EnabledIfEnvironmentVariable(named = "MOUSER_API_KEY", matches = ".+")
class MouserApiClientLiveTest {

    @Test
    fun `keyword search against the real API returns at least one part`() {
        val client = MouserApiClient(
            baseUrl = System.getenv("MOUSER_BASE_URL") ?: "https://api.mouser.com",
            apiKey = System.getenv("MOUSER_API_KEY"),
        )

        val response = client.searchKeyword("ESP32")

        assertTrue(response.parts.isNotEmpty(), "Expected at least one part from the real search.")
    }
}
