package org.gridmind.backend.catalog.infrastructure.provider.adafruit

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

/**
 * A real call against Adafruit's public Products API — skipped entirely unless
 * RUN_ADAFRUIT_LIVE_TEST=true is set, so `./gradlew test` never depends on network access,
 * even though this API needs no credentials at all. Run it deliberately, e.g.
 * `RUN_ADAFRUIT_LIVE_TEST=true ./gradlew test --tests "*AdafruitApiClientLiveTest*"`.
 */
@EnabledIfEnvironmentVariable(named = "RUN_ADAFRUIT_LIVE_TEST", matches = "true")
class AdafruitApiClientLiveTest {

    @Test
    fun `fetching the real catalog returns a large list of products`() {
        val client = AdafruitApiClient(baseUrl = System.getenv("ADAFRUIT_BASE_URL") ?: "https://www.adafruit.com")

        val products = client.allProducts()

        assertTrue(products.size > 1_000, "Expected several thousand products, got ${products.size}.")
    }

    @Test
    fun `isReachable succeeds against the real public API`() {
        val client = AdafruitApiClient(baseUrl = System.getenv("ADAFRUIT_BASE_URL") ?: "https://www.adafruit.com")

        assertTrue(client.isReachable())
    }
}
