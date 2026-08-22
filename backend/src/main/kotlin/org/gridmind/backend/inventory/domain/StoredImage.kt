package org.gridmind.backend.inventory.domain

/**
 * A downloaded copy of a product image, kept on local storage so an item's picture
 * doesn't depend on an external provider's URL staying reachable. Identified by
 * [checksum] so the same image found through different searches is only ever stored once.
 */
data class StoredImage(
    val id: Long? = null,
    val checksum: String,
    val contentType: String,
    val filePath: String,
    val sourceProvider: String? = null,
    val sourceUrl: String? = null,
) {
    init {
        require(checksum.isNotBlank()) { "Stored image checksum must not be blank." }
        require(contentType.isNotBlank()) { "Stored image content type must not be blank." }
        require(filePath.isNotBlank()) { "Stored image file path must not be blank." }
    }
}
