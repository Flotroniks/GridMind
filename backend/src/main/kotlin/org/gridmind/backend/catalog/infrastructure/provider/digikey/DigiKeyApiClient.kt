package org.gridmind.backend.catalog.infrastructure.provider.digikey

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.time.Instant

/**
 * Talks to the DigiKey Product Information v4 API: fetches and caches an OAuth2 client
 * credentials token, and runs keyword searches. Deliberately dumb — it only knows how to
 * call DigiKey and hand back its raw response shape; mapping to `CatalogResult` lives in
 * [DigiKeyProductCatalogProvider]. Any HTTP/auth failure is left to propagate — the
 * caller (`ProductSearchService`) already treats a provider throwing as "no results from
 * this one", so there's no need to duplicate that resilience here.
 *
 * Builds its own [RestClient] rather than taking an injected `RestClient.Builder` — this
 * project doesn't pull in a starter that autoconfigures one (see
 * [org.gridmind.backend.inventory.infrastructure.media.RestClientImageDownloader], which
 * does the same for the same reason). The Jackson converter is registered explicitly so
 * JSON (de)serialization doesn't depend on classpath auto-detection working out.
 */
class DigiKeyApiClient(
    baseUrl: String,
    private val clientId: String,
    private val clientSecret: String,
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

    /** Verifies credentials/connectivity via the OAuth2 token endpoint alone — DigiKey
     * documents this as separate from the Product Information API's per-day call quota,
     * so this is safe to call far more often than a real search (also benefits from the
     * same token cache [accessToken] already uses, so repeated calls within a token's
     * lifetime don't even hit the network). Never throws. */
    fun isReachable(): Boolean =
        try {
            accessToken()
            true
        } catch (ex: Exception) {
            false
        }

    fun searchKeyword(query: String): DigiKeyKeywordSearchResponse =
        restClient.post()
            .uri("/products/v4/search/keyword")
            .header("X-DIGIKEY-Client-Id", clientId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken()}")
            .contentType(MediaType.APPLICATION_JSON)
            .body(DigiKeyKeywordSearchRequest(keywords = query))
            .retrieve()
            .body(DigiKeyKeywordSearchResponse::class.java)
            ?: DigiKeyKeywordSearchResponse()

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
        val form = LinkedMultiValueMap<String, String>().apply {
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("grant_type", "client_credentials")
        }

        val response = restClient.post()
            .uri("/v1/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(DigiKeyTokenResponse::class.java)
            ?: error("DigiKey token endpoint returned an empty response.")

        // Refresh a bit before actual expiry so a request in flight never races an
        // expiring token.
        val usableSeconds = (response.expiresIn - 30).coerceAtLeast(30)
        return CachedToken(response.accessToken, Instant.now().plusSeconds(usableSeconds))
    }

    private class CachedToken(val value: String, private val expiresAt: Instant) {
        fun isStillValid(): Boolean = Instant.now().isBefore(expiresAt)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DigiKeyTokenResponse(
    @JsonProperty("access_token") val accessToken: String = "",
    @JsonProperty("expires_in") val expiresIn: Long = 0,
)

data class DigiKeyKeywordSearchRequest(
    @JsonProperty("Keywords") val keywords: String,
)

/** Only the fields GridMind actually maps to a `CatalogResult` — DigiKey's real response
 * carries far more (pricing, stock, parametric specs, ...), which we deliberately ignore. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DigiKeyKeywordSearchResponse(
    @JsonProperty("Products") val products: List<DigiKeyProduct> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DigiKeyProduct(
    @JsonProperty("ManufacturerProductNumber") val manufacturerProductNumber: String? = null,
    @JsonProperty("Description") val description: DigiKeyDescription? = null,
    @JsonProperty("Manufacturer") val manufacturer: DigiKeyManufacturer? = null,
    @JsonProperty("Category") val category: DigiKeyCategory? = null,
    @JsonProperty("DatasheetUrl") val datasheetUrl: String? = null,
    @JsonProperty("PhotoUrl") val photoUrl: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DigiKeyDescription(
    @JsonProperty("ProductDescription") val productDescription: String? = null,
    @JsonProperty("DetailedDescription") val detailedDescription: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DigiKeyManufacturer(
    @JsonProperty("Name") val name: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DigiKeyCategory(
    @JsonProperty("Name") val name: String? = null,
)
