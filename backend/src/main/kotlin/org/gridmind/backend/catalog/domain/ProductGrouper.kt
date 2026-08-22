package org.gridmind.backend.catalog.domain

/**
 * Merges catalog results that represent the same real-world product from different
 * providers into one enriched [CatalogResult].
 *
 * Grouping is deterministic — normalized manufacturer + normalized MPN — deliberately
 * with no fuzzy matching. A result missing a manufacturer still merges into an existing
 * group when its MPN matches and doing so wouldn't conflict with a manufacturer already
 * recorded on that group.
 */
object ProductGrouper {

    fun group(results: List<CatalogResult>): List<CatalogResult> {
        val groups = mutableListOf<MutableGroup>()

        for (result in results) {
            val normalizedMpn = normalize(result.mpn)
            val normalizedManufacturer = result.manufacturer?.let(::normalize)

            val match = groups.firstOrNull { group ->
                group.normalizedMpn == normalizedMpn &&
                    (group.normalizedManufacturer == null || normalizedManufacturer == null ||
                        group.normalizedManufacturer == normalizedManufacturer)
            }

            if (match != null) {
                match.merge(result, normalizedManufacturer)
            } else {
                groups += MutableGroup(result, normalizedMpn, normalizedManufacturer)
            }
        }

        return groups.map { it.toResult() }
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("[\\s-]+"), "")

    private class MutableGroup(
        first: CatalogResult,
        val normalizedMpn: String,
        var normalizedManufacturer: String?,
    ) {
        private val name = first.name
        private var manufacturer = first.manufacturer
        private val mpn = first.mpn
        private var description = first.description
        private var category = first.category
        private var datasheetUrl = first.datasheetUrl
        private val images = first.images.toMutableList()
        private val sources = first.sources.toMutableList()

        fun merge(result: CatalogResult, resultNormalizedManufacturer: String?) {
            if (manufacturer == null && result.manufacturer != null) {
                manufacturer = result.manufacturer
                normalizedManufacturer = resultNormalizedManufacturer
            }
            if (description == null) description = result.description
            if (category == null) category = result.category
            if (datasheetUrl == null) datasheetUrl = result.datasheetUrl
            for (image in result.images) if (image !in images) images += image
            for (source in result.sources) if (source !in sources) sources += source
        }

        fun toResult(): CatalogResult = CatalogResult(
            name = name,
            manufacturer = manufacturer,
            mpn = mpn,
            description = description,
            category = category,
            datasheetUrl = datasheetUrl,
            images = images.toList(),
            sources = sources.toList(),
        )
    }
}
