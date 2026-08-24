package org.gridmind.backend.catalog.infrastructure.provider.ebay

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

/**
 * A real call against eBay's production Buy Browse API — skipped entirely unless
 * EBAY_CLIENT_ID is set in the environment, so `./gradlew test` never depends on network
 * access or credentials. Run it deliberately once you've filled in EBAY_CLIENT_ID/
 * EBAY_CLIENT_SECRET (see .env.example) to confirm the real integration still works, e.g.
 * `EBAY_CLIENT_ID=... EBAY_CLIENT_SECRET=... ./gradlew test --tests "*EbayApiClientLiveTest*"`.
 */
@EnabledIfEnvironmentVariable(named = "EBAY_CLIENT_ID", matches = ".+")
class EbayApiClientLiveTest {

    @Test
    fun `keyword search against the real API returns at least one item`() {
        val client = EbayApiClient(
            baseUrl = System.getenv("EBAY_BASE_URL") ?: "https://api.ebay.com",
            clientId = System.getenv("EBAY_CLIENT_ID"),
            clientSecret = System.getenv("EBAY_CLIENT_SECRET")
                ?: error("EBAY_CLIENT_SECRET must be set alongside EBAY_CLIENT_ID."),
            marketplaceId = System.getenv("EBAY_MARKETPLACE_ID") ?: "EBAY_US",
        )

        val response = client.searchKeyword("ESP32")

        assertTrue(response.itemSummaries.isNotEmpty(), "Expected at least one item from the real search.")
    }

    @Test
    fun `isReachable succeeds against the real token endpoint`() {
        val client = EbayApiClient(
            baseUrl = System.getenv("EBAY_BASE_URL") ?: "https://api.ebay.com",
            clientId = System.getenv("EBAY_CLIENT_ID"),
            clientSecret = System.getenv("EBAY_CLIENT_SECRET")
                ?: error("EBAY_CLIENT_SECRET must be set alongside EBAY_CLIENT_ID."),
            marketplaceId = System.getenv("EBAY_MARKETPLACE_ID") ?: "EBAY_US",
        )

        assertTrue(client.isReachable())
    }
}
