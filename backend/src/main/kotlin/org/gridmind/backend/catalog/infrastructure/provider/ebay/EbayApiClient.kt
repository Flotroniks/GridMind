package org.gridmind.backend.catalog.infrastructure.provider.ebay

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.Base64

/**
 * Talks to eBay's Buy Browse API keyword search: fetches and caches an OAuth2
 * client-credentials token (Basic-auth'd, unlike DigiKey's form-encoded client
 * id/secret), and runs keyword searches scoped to one marketplace. Deliberately dumb —
 * it only knows how to call eBay and hand back its raw response shape; mapping to
 * `CatalogResult` lives in [EbayProductCatalogProvider]. Any HTTP/auth failure is left to
 * propagate — the caller (`ProductSearchService`) already treats a provider throwing as
 * "no results from this one".
 *
 * Builds its own [RestClient] for the same reason as
 * [org.gridmind.backend.catalog.infrastructure.provider.digikey.DigiKeyApiClient]: this
 * project doesn't pull in a starter that autoconfigures one.
 */
class EbayApiClient(
    baseUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val marketplaceId: String,
) {
    private val restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .configureMessageConverters { it.withJsonConverter(JacksonJsonHttpMessageConverter()) }
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(5_000)
                setReadTimeout(5_000)
            },
        )
        .build()

    @Volatile
    private var cachedToken: CachedToken? = null

    /** Verifies credentials/connectivity via the OAuth2 token endpoint alone — eBay
     * documents this as separate from the Buy Browse API's per-day call quota, so this is
     * safe to call far more often than a real search (also benefits from the same token
     * cache [accessToken] already uses, so repeated calls within a token's lifetime don't
     * even hit the network). Never throws. */
    fun isReachable(): Boolean =
        try {
            accessToken()
            true
        } catch (ex: Exception) {
            false
        }

    fun searchKeyword(query: String): EbayItemSummarySearchResponse =
        restClient.get()
            .uri("/buy/browse/v1/item_summary/search?q={query}&limit={limit}&fieldgroups={fieldgroups}", query, SEARCH_LIMIT, "MATCHING_ITEMS,EXTENDED")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken()}")
            .header("X-EBAY-C-MARKETPLACE-ID", marketplaceId)
            .retrieve()
            .body(EbayItemSummarySearchResponse::class.java)
            ?: EbayItemSummarySearchResponse()

    /** Reuses the current token until shortly before it expires; refetches once it's stale. */
    private fun accessToken(): String {
        cachedToken?.let { if (it.isStillValid()) return it.value }

        synchronized(this) {
            cachedToken?.let { if (it.isStillValid()) return it.value }
            val fresh = fetchToken()
            cachedToken = fresh
            return fresh.value
        }
    }

    private fun fetchToken(): CachedToken {
        val basicAuth = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("scope", "https://api.ebay.com/oauth/api_scope")
        }

        val response = restClient.post()
            .uri("/identity/v1/oauth2/token")
            .header(HttpHeaders.AUTHORIZATION, "Basic $basicAuth")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(EbayTokenResponse::class.java)
            ?: error("eBay token endpoint returned an empty response.")

        // Refresh a bit before actual expiry so a request in flight never races an
        // expiring token.
        val usableSeconds = (response.expiresIn - 30).coerceAtLeast(30)
        return CachedToken(response.accessToken, Instant.now().plusSeconds(usableSeconds))
    }

    private class CachedToken(val value: String, private val expiresAt: Instant) {
        fun isStillValid(): Boolean = Instant.now().isBefore(expiresAt)
    }

    companion object {
        private const val SEARCH_LIMIT = 10
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EbayTokenResponse(
    @JsonProperty("access_token") val accessToken: String = "",
    @JsonProperty("expires_in") val expiresIn: Long = 0,
)

/** Only the fields GridMind actually maps to a `CatalogResult` — eBay's real response
 * carries far more (seller, shipping, buying options, ...), deliberately ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EbayItemSummarySearchResponse(
    @JsonProperty("itemSummaries") val itemSummaries: List<EbayItemSummary> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EbayItemSummary(
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("shortDescription") val shortDescription: String? = null,
    @JsonProperty("condition") val condition: String? = null,
    @JsonProperty("price") val price: EbayConvertedAmount? = null,
    @JsonProperty("image") val image: EbayImage? = null,
    @JsonProperty("categories") val categories: List<EbayCategory>? = null,
    @JsonProperty("leafCategoryIds") val leafCategoryIds: List<String>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EbayConvertedAmount(
    @JsonProperty("value") val value: String? = null,
    @JsonProperty("currency") val currency: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EbayImage(
    @JsonProperty("imageUrl") val imageUrl: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EbayCategory(
    @JsonProperty("categoryId") val categoryId: String? = null,
    @JsonProperty("categoryName") val categoryName: String? = null,
)
