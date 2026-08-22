package org.gridmind.backend.storage.domain

/**
 * How many units of a given item are sitting at a given storage location.
 *
 * This exists as its own concept instead of a `locationId` on [org.gridmind.backend.inventory.domain.Item]
 * because an item isn't always in exactly one place — some of your resistors might be
 * in the parts drawer while the rest already migrated to a project bin. The sum of all
 * [ItemStock] rows for an item is enforced (in [org.gridmind.backend.storage.application.StockAllocationService])
 * to never exceed that item's total quantity.
 */
data class ItemStock(
    val id: Long? = null,
    val itemId: Long,
    val storageLocationId: Long,
    val quantity: Int,
) {
    init {
        require(quantity >= 0) { "Item stock quantity must be greater than or equal to 0." }
    }
}
