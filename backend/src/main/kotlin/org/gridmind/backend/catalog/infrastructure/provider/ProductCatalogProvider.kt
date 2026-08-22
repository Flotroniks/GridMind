package org.gridmind.backend.catalog.infrastructure.provider

import org.gridmind.backend.catalog.domain.CatalogResult

/** A source of product search results — one implementation per external catalog (DigiKey, Mouser, ...). */
interface ProductCatalogProvider {
    val name: String
    fun search(query: String): List<CatalogResult>
}
