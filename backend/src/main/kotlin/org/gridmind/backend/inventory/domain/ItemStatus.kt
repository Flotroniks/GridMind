package org.gridmind.backend.inventory.domain

/** Whether an item is currently usable ([IN_SERVICE]) or broken/unusable ([OUT_OF_SERVICE]). */
enum class ItemStatus {
    IN_SERVICE,
    OUT_OF_SERVICE,
}
