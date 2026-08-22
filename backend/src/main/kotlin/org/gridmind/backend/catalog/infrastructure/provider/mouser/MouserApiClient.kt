package org.gridmind.backend.catalog.infrastructure.provider.mouser

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.RestClient

/**
 * Talks to Mouser's Search API v1 keyword search. Unlike DigiKey, auth is a single API
 * key passed as a query parameter — no OAuth2 token dance needed, so there's no token
 * cache here. Mapping to `CatalogResult` lives in [MouserProductCatalogProvider]; any
 * HTTP failure is left to propagate — the caller (`ProductSearchService`) already
 * treats a provider throwing as "no results from this one".
 *
 * Builds its own [RestClient] for the same reason as
 * [org.gridmind.backend.catalog.infrastructure.provider.digikey.DigiKeyApiClient]: this
 * project doesn't pull in a starter that autoconfigures one.
 */
class MouserApiClient(
    baseUrl: String,
    private val apiKey: String,
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

    fun searchKeyword(query: String): MouserKeywordSearchResponse =
        restClient.post()
            .uri("/api/v1/search/keyword?apiKey={apiKey}", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(MouserKeywordSearchRequestRoot(MouserKeywordSearchRequest(keyword = query, records = 10)))
            .retrieve()
            .body(MouserKeywordSearchResponse::class.java)
            ?: MouserKeywordSearchResponse()
}

data class MouserKeywordSearchRequestRoot(
    @JsonProperty("SearchByKeywordRequest") val searchByKeywordRequest: MouserKeywordSearchRequest,
)

data class MouserKeywordSearchRequest(
    @JsonProperty("keyword") val keyword: String,
    @JsonProperty("records") val records: Int? = null,
)

/** Only the fields GridMind actually maps to a `CatalogResult` — Mouser's real response
 * carries far more (pricing, stock, lifecycle, compliance, ...), deliberately ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MouserKeywordSearchResponse(
    @JsonProperty("SearchResults") val searchResults: MouserSearchResults? = null,
) {
    val parts: List<MouserPart> get() = searchResults?.parts ?: emptyList()
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MouserSearchResults(
    @JsonProperty("Parts") val parts: List<MouserPart> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MouserPart(
    @JsonProperty("ManufacturerPartNumber") val manufacturerPartNumber: String? = null,
    @JsonProperty("Description") val description: String? = null,
    @JsonProperty("Manufacturer") val manufacturer: String? = null,
    @JsonProperty("Category") val category: String? = null,
    @JsonProperty("DataSheetUrl") val dataSheetUrl: String? = null,
    @JsonProperty("ImagePath") val imagePath: String? = null,
)
