package org.gridmind.backend.category.domain

/**
 * A user-defined inventory category, e.g. "Sensors" or "Microcontrollers".
 *
 * Deliberately just a name rather than a fixed enum — the point of categories is that
 * people can add their own as their workshop's needs don't match a preset list.
 */
data class Category(
    val id: Long? = null,
    val name: String,
) {
    init {
        require(name.isNotBlank()) { "Category name must not be blank." }
    }
}
