package org.gridmind.backend.inventory.domain

/**
 * A physical item tracked in the workshop inventory — a component, tool, or part.
 *
 * [categoryId] is nullable because items can exist before they're categorized, and
 * where an item is physically stored deliberately isn't a field here: that's modeled
 * separately in the storage module, since one item can live in several locations at once.
 *
 * [quantity] is the total owned. Of those, [quantityHs] are broken/unusable and
 * [quantityInUse] are currently tied up in a project — neither is available to grab.
 * [quantityAvailable] is what's actually left to use right now.
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
    val quantityHs: Int = 0,
    val quantityInUse: Int = 0,
) {
    init {
        require(name.isNotBlank()) { "Item name must not be blank." }
        require(quantity >= 0) { "Item quantity must be greater than or equal to 0." }
        require(minimumQuantity >= 0) { "Item minimum quantity must be greater than or equal to 0." }
        require(quantityHs >= 0) { "Item HS quantity must be greater than or equal to 0." }
        require(quantityInUse >= 0) { "Item in-use quantity must be greater than or equal to 0." }
        require(quantityHs + quantityInUse <= quantity) {
            "Item HS and in-use quantities must not exceed total quantity."
        }
    }

    val quantityAvailable: Int get() = quantity - quantityHs - quantityInUse
}
