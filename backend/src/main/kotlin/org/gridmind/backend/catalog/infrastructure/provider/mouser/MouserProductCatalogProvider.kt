package org.gridmind.backend.catalog.infrastructure.provider.mouser

import org.gridmind.backend.catalog.domain.CatalogImage
import org.gridmind.backend.catalog.domain.CatalogResult
import org.gridmind.backend.catalog.infrastructure.provider.ProductCatalogProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

/**
 * Searches Mouser's Search API v1 (keyword search). Only registered as a bean when an
 * API key is actually configured — see `application.yaml` and `.env.example`. Without
 * it, [org.gridmind.backend.catalog.application.ProductSearchService] simply sees one
 * fewer provider in its list; nothing else needs to know Mouser exists.
 */
@Component
@ConditionalOnExpression("'\${gridmind.catalog.mouser.api-key:}'.length() > 0")
class MouserProductCatalogProvider(
    @Value("\${gridmind.catalog.mouser.base-url}") baseUrl: String,
    @Value("\${gridmind.catalog.mouser.api-key}") apiKey: String,
) : ProductCatalogProvider {

    override val name = "Mouser"

    private val client = MouserApiClient(baseUrl, apiKey)

    override fun search(query: String): List<CatalogResult> =
        client.searchKeyword(query).parts.mapNotNull { it.toCatalogResult(name) }
}

/**
 * A part missing its MPN can't be mapped (it's the one field GridMind requires) and is
 * silently skipped. Kept as a standalone function (not a method) so it's testable
 * without spinning up a client or a Spring context — same pattern as DigiKey's
 * `DigiKeyProduct.toCatalogResult`.
 */
internal fun MouserPart.toCatalogResult(providerName: String): CatalogResult? {
    val mpn = manufacturerPartNumber?.takeIf { it.isNotBlank() } ?: return null
    val resultName = description?.takeIf { it.isNotBlank() } ?: mpn

    return CatalogResult(
        name = resultName,
        manufacturer = manufacturer,
        mpn = mpn,
        description = description,
        category = category,
        datasheetUrl = dataSheetUrl,
        images = imagePath?.takeIf { it.isNotBlank() }
            ?.let { listOf(CatalogImage(url = it, provider = providerName)) }
            ?: emptyList(),
        sources = listOf(providerName),
    )
}
