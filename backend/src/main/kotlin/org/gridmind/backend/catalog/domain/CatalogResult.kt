package org.gridmind.backend.catalog.domain

/**
 * A product found in an external parts catalog (DigiKey, Mouser, ...), normalized to a
 * shape GridMind understands. [sources] lists which providers contributed to this result
 * after grouping — a single [CatalogResult] can carry data merged from several providers.
 */
data class CatalogResult(
    val name: String,
    val manufacturer: String?,
    val mpn: String,
    val description: String? = null,
    val category: String? = null,
    val datasheetUrl: String? = null,
    val images: List<CatalogImage> = emptyList(),
    val sources: List<String> = emptyList(),
) {
    init {
        require(name.isNotBlank()) { "Catalog result name must not be blank." }
        require(mpn.isNotBlank()) { "Catalog result MPN must not be blank." }
    }
}

/** One image candidate for a [CatalogResult], still hosted externally at [url] by [provider]. */
data class CatalogImage(
    val url: String,
    val provider: String,
)
