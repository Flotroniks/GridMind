package org.gridmind.backend.catalog.api

import org.gridmind.backend.catalog.application.ProductSearchService
import org.gridmind.backend.catalog.domain.CatalogImage
import org.gridmind.backend.catalog.domain.CatalogResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val productSearchService: ProductSearchService,
) {
    @GetMapping("/search")
    fun search(@RequestParam query: String): List<CatalogResultResponse> {
        require(query.isNotBlank()) { "Query must not be blank." }
        return productSearchService.search(query).map(CatalogResultResponse::from)
    }
}

data class CatalogImageResponse(
    val url: String,
    val provider: String,
) {
    companion object {
        fun from(image: CatalogImage): CatalogImageResponse =
            CatalogImageResponse(url = image.url, provider = image.provider)
    }
}

data class CatalogResultResponse(
    val name: String,
    val manufacturer: String?,
    val mpn: String?,
    val description: String?,
    val category: String?,
    val datasheetUrl: String?,
    val images: List<CatalogImageResponse>,
    val sources: List<String>,
) {
    companion object {
        fun from(result: CatalogResult): CatalogResultResponse = CatalogResultResponse(
            name = result.name,
            manufacturer = result.manufacturer,
            mpn = result.mpn,
            description = result.description,
            category = result.category,
            datasheetUrl = result.datasheetUrl,
            images = result.images.map(CatalogImageResponse::from),
            sources = result.sources,
        )
    }
}
