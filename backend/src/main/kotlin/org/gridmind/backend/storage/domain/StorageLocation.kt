package org.gridmind.backend.storage.domain

/**
 * A physical storage location, optionally nested under a parent — e.g. a drawer inside
 * a cabinet inside the workshop.
 *
 * This only models the hierarchy. How much of which item sits here is tracked
 * separately by [ItemStock], since a location can hold many items and an item can be
 * split across several locations.
 *
 * [ledControllerId] and [ledIndex] are an optional mapping onto a physical pick-to-light
 * LED: which addressable strip/controller it's wired to, and its pixel index on that
 * strip. This is deliberately kept in the database rather than baked into any firmware —
 * the microcontroller is just told "light pixel N on this controller with this color"
 * (see the locate feature), so a dead controller can be swapped without reflashing any
 * location-specific logic. Both are null until a location is wired up and configured.
 */
data class StorageLocation(
    val id: Long? = null,
    val name: String,
    val parentId: Long? = null,
    val ledControllerId: String? = null,
    val ledIndex: Int? = null,
) {
    init {
        require(name.isNotBlank()) { "Storage location name must not be blank." }
        require((ledControllerId == null) == (ledIndex == null)) {
            "ledControllerId and ledIndex must be set or cleared together."
        }
        require(ledIndex == null || ledIndex >= 0) { "ledIndex must not be negative." }
    }
}
