package org.gridmind.backend.storage.domain

/**
 * A physical storage location, optionally nested under a parent — e.g. a drawer inside
 * a cabinet inside the workshop.
 *
 * This only models the hierarchy. How much of which item sits here is tracked
 * separately by [ItemStock], since a location can hold many items and an item can be
 * split across several locations.
 */
data class StorageLocation(
    val id: Long? = null,
    val name: String,
    val parentId: Long? = null,
) {
    init {
        require(name.isNotBlank()) { "Storage location name must not be blank." }
    }
}
