package org.gridmind.backend.locate.domain

/** One storage location to highlight, and which color to highlight it with. */
data class LocateHighlight(
    val storageLocationId: Long,
    val storageLocationName: String,
    val color: String,
)
