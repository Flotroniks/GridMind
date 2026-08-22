package org.gridmind.backend.catalog.domain

/**
 * A product found in an external parts catalog (DigiKey, Mouser, ...), normalized to a
 * shape GridMind understands. [sources] lists which providers contributed to this result
 * after grouping — a single [CatalogResult] can carry data merged from several providers.
 *
 * [mpn] is deliberately optional. Electronic component distributors always have one, but
 * maker hardware (a Wemos D1 Mini, a generic breakout board, ...) often doesn't — there's
 * no manufacturer-assigned part number to key off. [ProductGrouper] only ever merges two
 * results when both have a matching [mpn]; a result without one is never merged with
 * anything and is always shown as its own entry.
 */
data class CatalogResult(
    val name: String,
    val manufacturer: String?,
    val mpn: String? = null,
    val description: String? = null,
    val category: String? = null,
    val datasheetUrl: String? = null,
    val images: List<CatalogImage> = emptyList(),
    val sources: List<String> = emptyList(),
) {
    init {
        require(name.isNotBlank()) { "Catalog result name must not be blank." }
        require(mpn == null || mpn.isNotBlank()) { "Catalog result MPN must not be blank when present." }
    }
}

/** One image candidate for a [CatalogResult], still hosted externally at [url] by [provider]. */
data class CatalogImage(
    val url: String,
    val provider: String,
)
