package org.gridmind.backend.catalog.infrastructure.provider.ebay

import org.gridmind.backend.catalog.application.CatalogProviderHealthCheck
import org.gridmind.backend.catalog.application.ProductCatalogProvider
import org.gridmind.backend.catalog.domain.CatalogImage
import org.gridmind.backend.catalog.domain.CatalogResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

/**
 * Searches eBay's Buy Browse API (keyword search). Only registered as a bean when both
 * credentials are actually configured — see `application.yaml` and `.env.example`.
 * Without them, [org.gridmind.backend.catalog.application.ProductSearchService] simply
 * sees one fewer provider in its list; nothing else needs to know eBay exists.
 */
@Component
@ConditionalOnExpression(
    "'\${gridmind.catalog.ebay.client-id:}'.length() > 0 and " +
        "'\${gridmind.catalog.ebay.client-secret:}'.length() > 0",
)
class EbayProductCatalogProvider(
    @Value("\${gridmind.catalog.ebay.base-url}") baseUrl: String,
    @Value("\${gridmind.catalog.ebay.client-id}") clientId: String,
    @Value("\${gridmind.catalog.ebay.client-secret}") clientSecret: String,
    @Value("\${gridmind.catalog.ebay.marketplace-id}") marketplaceId: String,
) : ProductCatalogProvider, CatalogProviderHealthCheck {

    override val name = "eBay"

    private val client = EbayApiClient(baseUrl, clientId, clientSecret, marketplaceId)

    override fun search(query: String): List<CatalogResult> =
        client.searchKeyword(query).itemSummaries.mapNotNull { it.toCatalogResult(name) }

    override fun checkHealth(): Boolean = client.isReachable()
}

/**
 * eBay listings are fundamentally different from a distributor's catalog: there's no
 * manufacturer/MPN in the search response at all (only available, unreliably, via a
 * separate per-item call this adapter doesn't make — not worth an extra HTTP round trip
 * per result just for an optional field). `CatalogResult.mpn`/`manufacturer` being
 * optional at the model level is exactly what makes this provider possible without
 * distorting the common model: a listing is always kept as its own, unmerged entry (see
 * `ProductGrouper`), never guessed into a distributor's part.
 *
 * Kept as a standalone function (not a method) so it's testable without spinning up a
 * client or a Spring context — same pattern as DigiKey's, Mouser's and Adafruit's mapping
 * functions.
 */
internal fun EbayItemSummary.toCatalogResult(providerName: String): CatalogResult? {
    val resultName = title?.takeIf { it.isNotBlank() } ?: return null

    return CatalogResult(
        name = resultName,
        manufacturer = null,
        mpn = null,
        description = shortDescription?.takeIf { it.isNotBlank() } ?: conditionAndPrice(),
        category = leafCategoryName(),
        datasheetUrl = null,
        images = image?.imageUrl?.takeIf { it.isNotBlank() }
            ?.let { listOf(CatalogImage(url = it, provider = providerName)) }
            ?: emptyList(),
        sources = listOf(providerName),
    )
}

/**
 * `categories` mixes top-level, branch and leaf categories with no documented ordering
 * guarantee, so the most specific one is found by matching against `leafCategoryIds`
 * instead of trusting array position; falls back to the last entry if that match fails.
 */
private fun EbayItemSummary.leafCategoryName(): String? {
    val leafIds = leafCategoryIds.orEmpty()
    return categories?.firstOrNull { it.categoryId != null && it.categoryId in leafIds }?.categoryName
        ?: categories?.lastOrNull()?.categoryName
}

/** Falls back to "condition — price currency" when eBay doesn't return a `shortDescription`. */
private fun EbayItemSummary.conditionAndPrice(): String? {
    val priceText = price?.value?.takeIf { it.isNotBlank() }
        ?.let { value -> price.currency?.takeIf { it.isNotBlank() }?.let { currency -> "$value $currency" } ?: value }

    return listOfNotNull(condition?.takeIf { it.isNotBlank() }, priceText)
        .joinToString(" — ")
        .takeIf { it.isNotBlank() }
}
