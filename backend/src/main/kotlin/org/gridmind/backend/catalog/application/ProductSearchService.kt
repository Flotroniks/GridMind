package org.gridmind.backend.catalog.application

import org.gridmind.backend.catalog.domain.CatalogResult
import org.gridmind.backend.catalog.domain.ProductGrouper
import org.gridmind.backend.catalog.infrastructure.provider.ProductCatalogProvider
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.concurrent.Executors

/**
 * Fans a search query out to every configured [ProductCatalogProvider] in parallel and
 * merges what comes back with [ProductGrouper]. A provider that times out, errors, or
 * runs out of quota just contributes nothing — it never stops the others from answering.
 */
@Service
class ProductSearchService(
    private val providers: List<ProductCatalogProvider>,
) {
    private val logger = LoggerFactory.getLogger(ProductSearchService::class.java)

    @Cacheable(value = ["catalogSearch"], key = "#query.trim().toLowerCase()")
    fun search(query: String): List<CatalogResult> {
        val normalizedQuery = query.trim()

        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val tasks = providers.map { provider ->
                executor.submit<List<CatalogResult>> { searchSafely(provider, normalizedQuery) }
            }

            val results = tasks.flatMap { it.get() }
            return ProductGrouper.group(results)
        }
    }

    private fun searchSafely(provider: ProductCatalogProvider, query: String): List<CatalogResult> =
        try {
            provider.search(query)
        } catch (ex: Exception) {
            logger.warn("Provider '{}' failed to answer search '{}': {}", provider.name, query, ex.message)
            emptyList()
        }
}
