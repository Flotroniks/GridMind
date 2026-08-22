package org.gridmind.backend.catalog.infrastructure.provider.digikey

import org.gridmind.backend.catalog.domain.CatalogImage
import org.gridmind.backend.catalog.domain.CatalogResult
import org.gridmind.backend.catalog.infrastructure.provider.ProductCatalogProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

/**
 * Searches DigiKey's Product Information v4 API (keyword search). Only registered as a
 * bean when both credentials are actually configured — see `application.yaml` and
 * `.env.example`. Without them, [org.gridmind.backend.catalog.application.ProductSearchService]
 * simply sees one fewer provider in its list; nothing else needs to know DigiKey exists.
 */
@Component
@ConditionalOnExpression(
    "'\${gridmind.catalog.digikey.client-id:}'.length() > 0 and " +
        "'\${gridmind.catalog.digikey.client-secret:}'.length() > 0",
)
class DigiKeyProductCatalogProvider(
    @Value("\${gridmind.catalog.digikey.base-url}") baseUrl: String,
    @Value("\${gridmind.catalog.digikey.client-id}") clientId: String,
    @Value("\${gridmind.catalog.digikey.client-secret}") clientSecret: String,
) : ProductCatalogProvider {

    override val name = "DigiKey"

    private val client = DigiKeyApiClient(baseUrl, clientId, clientSecret)

    override fun search(query: String): List<CatalogResult> =
        client.searchKeyword(query).products.mapNotNull { it.toCatalogResult(name) }
}

/**
 * A product missing its MPN can't be mapped (it's the one field GridMind requires) and is
 * silently skipped — DigiKey's search occasionally returns sparse/legacy records like that.
 * Kept as a standalone function (not a method) so it's testable without spinning up a
 * client or a Spring context.
 */
internal fun DigiKeyProduct.toCatalogResult(providerName: String): CatalogResult? {
    val mpn = manufacturerProductNumber?.takeIf { it.isNotBlank() } ?: return null
    val resultName = description?.productDescription?.takeIf { it.isNotBlank() } ?: mpn

    return CatalogResult(
        name = resultName,
        manufacturer = manufacturer?.name,
        mpn = mpn,
        description = description?.detailedDescription?.takeIf { it.isNotBlank() }
            ?: description?.productDescription,
        category = category?.name,
        datasheetUrl = datasheetUrl,
        images = photoUrl?.takeIf { it.isNotBlank() }
            ?.let { listOf(CatalogImage(url = it, provider = providerName)) }
            ?: emptyList(),
        sources = listOf(providerName),
    )
}
