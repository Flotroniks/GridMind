package org.gridmind.backend.catalog.application

import org.gridmind.backend.catalog.domain.CatalogResult
import org.gridmind.backend.catalog.domain.ProductGrouper
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

    /**
     * Runs several queries in parallel — used when the AI image analysis suggests more
     * than one candidate search term — and merges everything into one deduplicated list
     * via [ProductGrouper]: a product found by two different queries collapses into one
     * entry, exactly as one found by two different providers already does in [search].
     *
     * Calls [search] directly rather than through the Spring proxy (self-invocation), so
     * the per-query cache doesn't apply here — acceptable since this path only ever runs
     * once per AI analysis, not as a repeated identical search.
     */
    fun searchMany(queries: List<String>): List<CatalogResult> {
        val normalizedQueries = queries.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (normalizedQueries.isEmpty()) return emptyList()

        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val tasks = normalizedQueries.map { query -> executor.submit<List<CatalogResult>> { search(query) } }
            return ProductGrouper.group(tasks.flatMap { it.get() })
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
