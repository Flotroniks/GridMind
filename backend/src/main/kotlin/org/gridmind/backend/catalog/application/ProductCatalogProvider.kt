package org.gridmind.backend.catalog.application

import org.gridmind.backend.catalog.domain.CatalogResult

/**
 * A source of product search results — one implementation per external catalog (DigiKey,
 * Mouser, ...). This is the port [ProductSearchService] depends on; concrete adapters live
 * under `catalog.infrastructure.provider`, one subpackage per catalog.
 */
interface ProductCatalogProvider {
    val name: String
    fun search(query: String): List<CatalogResult>
}
