package org.gridmind.backend.inventory.domain

/**
 * A physical item tracked in the workshop inventory — a component, tool, or part.
 *
 * [categoryId] is nullable because items can exist before they're categorized, and
 * where an item is physically stored deliberately isn't a field here: that's modeled
 * separately in the storage module, since one item can live in several locations at once.
 */
data class Item(
    val id: Long? = null,
    val name: String,
    val quantity: Int,
    val description: String? = null,
    val manufacturer: String? = null,
    val reference: String? = null,
    val categoryId: Long? = null,
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val productUrl: String? = null,
    val datasheetUrl: String? = null,
    val minimumQuantity: Int = 0,
    val status: ItemStatus = ItemStatus.IN_SERVICE,
) {
    init {
        require(name.isNotBlank()) { "Item name must not be blank." }
        require(quantity >= 0) { "Item quantity must be greater than or equal to 0." }
        require(minimumQuantity >= 0) { "Item minimum quantity must be greater than or equal to 0." }
    }
}
