package org.gridmind.backend.locate.domain

/**
 * One storage location to highlight, and which color to highlight it with.
 *
 * [ledControllerId] and [ledIndex] are the location's configured physical LED address
 * (see [org.gridmind.backend.storage.domain.StorageLocation]), carried through so a
 * subscribing microcontroller can act on this directly without knowing anything about
 * locations, items, or search — null when the location has no LED configured.
 */
data class LocateHighlight(
    val storageLocationId: Long,
    val storageLocationName: String,
    val color: String,
    val ledControllerId: String? = null,
    val ledIndex: Int? = null,
)
