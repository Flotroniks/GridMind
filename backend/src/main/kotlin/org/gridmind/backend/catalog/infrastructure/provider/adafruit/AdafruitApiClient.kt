package org.gridmind.backend.catalog.infrastructure.provider.adafruit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.RestClient
import java.time.Duration
import java.time.Instant

/**
 * Talks to Adafruit's public Products API. Unlike DigiKey/Mouser, this API has no
 * keyword-search endpoint at all — only a full-catalog dump (`/api/products`, ~5,500
 * products, several MB). So instead of one HTTP call per search, this client fetches the
 * whole catalog and keeps it in memory, refreshing it once the cached copy goes stale;
 * [AdafruitProductCatalogProvider] does the actual keyword filtering against that cache.
 * No API key is required (Adafruit's API is public), so there's no auth to manage here —
 * only a bigger payload and a longer read timeout than the other clients need.
 */
class AdafruitApiClient(baseUrl: String) {
    private val restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .configureMessageConverters { it.withJsonConverter(JacksonJsonHttpMessageConverter()) }
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(5_000)
                setReadTimeout(20_000)
            },
        )
        .build()

    @Volatile
    private var cachedCatalog: CachedCatalog? = null

    /** Reuses the in-memory catalog until it goes stale; refetches the full list once it does. */
    fun allProducts(): List<AdafruitProduct> {
        cachedCatalog?.let { if (it.isStillValid()) return it.products }

        synchronized(this) {
            cachedCatalog?.let { if (it.isStillValid()) return it.products }
            val fresh = fetchAllProducts()
            cachedCatalog = CachedCatalog(fresh, Instant.now().plus(CATALOG_TTL))
            return fresh
        }
    }

    private fun fetchAllProducts(): List<AdafruitProduct> =
        restClient.get()
            .uri("/api/products")
            .retrieve()
            .body(object : ParameterizedTypeReference<List<AdafruitProduct>>() {})
            ?: emptyList()

    private class CachedCatalog(val products: List<AdafruitProduct>, private val expiresAt: Instant) {
        fun isStillValid(): Boolean = Instant.now().isBefore(expiresAt)
    }

    companion object {
        private val CATALOG_TTL: Duration = Duration.ofHours(12)
    }
}

/** Only the fields GridMind actually maps to a `CatalogResult` — Adafruit's real response
 * carries far more (pricing, shipping weight, compliance codes, ...), deliberately ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdafruitProduct(
    @JsonProperty("product_name") val productName: String? = null,
    @JsonProperty("product_model") val productModel: String? = null,
    @JsonProperty("product_mpn") val productMpn: String? = null,
    @JsonProperty("product_manufacturer") val productManufacturer: String? = null,
    @JsonProperty("product_image") val productImage: String? = null,
    @JsonProperty("discontinue_status") val discontinueStatus: String? = null,
    @JsonProperty("products_virtual") val productsVirtual: String? = null,
)
