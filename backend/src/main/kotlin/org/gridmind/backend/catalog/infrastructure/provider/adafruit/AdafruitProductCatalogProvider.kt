package org.gridmind.backend.catalog.infrastructure.provider.adafruit

import org.gridmind.backend.catalog.application.CatalogProviderHealthCheck
import org.gridmind.backend.catalog.application.ProductCatalogProvider
import org.gridmind.backend.catalog.domain.CatalogImage
import org.gridmind.backend.catalog.domain.CatalogResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Searches Adafruit's public product catalog. Unlike DigiKey/Mouser, this needs no
 * credentials at all, so it's on by default — gated by a plain enabled flag instead of a
 * credential check, so it can still be turned off (e.g. to skip fetching/caching
 * Adafruit's multi-MB product feed in a constrained environment).
 */
@Component
@ConditionalOnProperty(
    prefix = "gridmind.catalog.adafruit",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class AdafruitProductCatalogProvider(
    @Value("\${gridmind.catalog.adafruit.base-url}") baseUrl: String,
) : ProductCatalogProvider, CatalogProviderHealthCheck {

    override val name = "Adafruit"

    private val client = AdafruitApiClient(baseUrl)

    override fun search(query: String): List<CatalogResult> =
        client.allProducts()
            .asSequence()
            .filter { it.isCatalogable() && it.matches(query) }
            .take(MAX_RESULTS)
            .mapNotNull { product ->
                val categoryName = product.productMasterCategory?.let(client::categoryName)
                product.toCatalogResult(name, categoryName)
            }
            .toList()

    override fun checkHealth(): Boolean = client.isReachable()

    companion object {
        private const val MAX_RESULTS = 20
    }
}

/** Excludes discontinued/pending listings and virtual (non-physical) products from search results. */
internal fun AdafruitProduct.isCatalogable(): Boolean =
    discontinueStatus == "None" && productsVirtual != "1"

internal fun AdafruitProduct.matches(query: String): Boolean =
    productName?.contains(query, ignoreCase = true) == true ||
        productMpn?.contains(query, ignoreCase = true) == true ||
        productModel?.contains(query, ignoreCase = true) == true

/**
 * `CatalogResult.mpn` is optional at the model level (maker hardware often has none), but
 * Adafruit's feed always carries its own store SKU (e.g. "ADA5800") — not a
 * manufacturer-issued MPN in the DigiKey/Mouser sense, but a stable per-product reference,
 * so it's mapped as the MPN rather than left null. Kept as a standalone function (not a
 * method) so it's testable without spinning up a client or a Spring context — same
 * pattern as DigiKey's and Mouser's mapping functions.
 *
 * Adafruit's feed has no product description field and no datasheet URL. `categoryName`
 * is resolved separately by the caller (see [AdafruitApiClient.categoryName]) since doing
 * so needs a network call — this function stays a pure, client-free mapping so it's
 * testable on its own, same as DigiKey's and Mouser's.
 */
internal fun AdafruitProduct.toCatalogResult(providerName: String, categoryName: String? = null): CatalogResult? {
    val resultName = productName?.takeIf { it.isNotBlank() } ?: return null

    return CatalogResult(
        name = resultName,
        manufacturer = productManufacturer?.takeIf { it.isNotBlank() } ?: "Adafruit",
        mpn = productMpn?.takeIf { it.isNotBlank() },
        description = productModel?.takeIf { it.isNotBlank() },
        category = categoryName,
        datasheetUrl = null,
        images = productImage?.takeIf { it.isNotBlank() }
            ?.let { listOf(CatalogImage(url = it, provider = providerName)) }
            ?: emptyList(),
        sources = listOf(providerName),
    )
}
