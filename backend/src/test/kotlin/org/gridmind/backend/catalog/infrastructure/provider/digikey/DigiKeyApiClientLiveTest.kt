package org.gridmind.backend.catalog.infrastructure.provider.digikey

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

/**
 * A real call against DigiKey's sandbox — skipped entirely unless DIGIKEY_CLIENT_ID is
 * set in the environment, so `./gradlew test` never depends on network access or
 * credentials. Run it deliberately once you've filled in DIGIKEY_CLIENT_ID/
 * DIGIKEY_CLIENT_SECRET (see .env.example) to confirm the real integration still works,
 * e.g. `DIGIKEY_CLIENT_ID=... DIGIKEY_CLIENT_SECRET=... ./gradlew test --tests
 * "*DigiKeyApiClientLiveTest*"`.
 */
@EnabledIfEnvironmentVariable(named = "DIGIKEY_CLIENT_ID", matches = ".+")
class DigiKeyApiClientLiveTest {

    @Test
    fun `keyword search against the real sandbox returns at least one product`() {
        val client = DigiKeyApiClient(
            baseUrl = System.getenv("DIGIKEY_BASE_URL") ?: "https://sandbox-api.digikey.com",
            clientId = System.getenv("DIGIKEY_CLIENT_ID"),
            clientSecret = System.getenv("DIGIKEY_CLIENT_SECRET")
                ?: error("DIGIKEY_CLIENT_SECRET must be set alongside DIGIKEY_CLIENT_ID."),
        )

        val response = client.searchKeyword("ESP32")

        assertTrue(response.products.isNotEmpty(), "Expected at least one product from the sandbox search.")
    }

    @Test
    fun `isReachable succeeds against the real token endpoint`() {
        val client = DigiKeyApiClient(
            baseUrl = System.getenv("DIGIKEY_BASE_URL") ?: "https://sandbox-api.digikey.com",
            clientId = System.getenv("DIGIKEY_CLIENT_ID"),
            clientSecret = System.getenv("DIGIKEY_CLIENT_SECRET")
                ?: error("DIGIKEY_CLIENT_SECRET must be set alongside DIGIKEY_CLIENT_ID."),
        )

        assertTrue(client.isReachable())
    }
}
