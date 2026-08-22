package org.gridmind.backend.storage.application

import org.gridmind.backend.inventory.infrastructure.persistence.ItemRepository
import org.gridmind.backend.shared.error.InsufficientStockException
import org.gridmind.backend.shared.error.ItemNotFoundException
import org.gridmind.backend.shared.error.StorageLocationNotFoundException
import org.gridmind.backend.storage.domain.ItemStock
import org.gridmind.backend.storage.infrastructure.persistence.ItemStockEntity
import org.gridmind.backend.storage.infrastructure.persistence.ItemStockRepository
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Owns the one rule that actually matters for this domain: the stock allocated across
 * all of an item's locations can never exceed the item's total quantity. There's no
 * database constraint that can express that (it's a cross-row sum), so it's enforced
 * here in the service before any [org.gridmind.backend.storage.infrastructure.persistence.ItemStockEntity]
 * row is written.
 */
@Service
class StockAllocationService(
    private val itemStockRepository: ItemStockRepository,
    private val itemRepository: ItemRepository,
    private val storageLocationRepository: StorageLocationRepository,
) {
    @Transactional(readOnly = true)
    fun stockByItem(itemId: Long): List<ItemStock> =
        itemStockRepository.findByItemId(itemId).map(ItemStockEntity::toDomain)

    @Transactional(readOnly = true)
    fun stockByLocation(storageLocationId: Long): List<ItemStock> =
        itemStockRepository.findByStorageLocationId(storageLocationId).map(ItemStockEntity::toDomain)

    /**
     * Sets how much of [itemId] is at [storageLocationId], creating or overwriting that
     * one row. [quantity] here is absolute, not a delta — to add stock incrementally,
     * read the current amount first and pass the new total.
     */
    @Transactional
    fun allocate(itemId: Long, storageLocationId: Long, quantity: Int): ItemStock {
        require(quantity >= 0) { "Item stock quantity must be greater than or equal to 0." }

        val item = itemRepository.findById(itemId).orElseThrow { ItemNotFoundException(itemId) }
        val location = storageLocationRepository.findById(storageLocationId)
            .orElseThrow { StorageLocationNotFoundException(storageLocationId) }

        val existing = itemStockRepository.findByItemIdAndStorageLocationId(itemId, storageLocationId)
        val alreadyAllocatedElsewhere = itemStockRepository.sumQuantityByItemId(itemId) - (existing?.quantity ?: 0)

        if (alreadyAllocatedElsewhere + quantity > item.quantity) {
            throw InsufficientStockException(
                "Cannot allocate $quantity units of item $itemId: only " +
                    "${item.quantity - alreadyAllocatedElsewhere} unit(s) available to allocate.",
            )
        }

        val stockEntity = existing?.apply { this.quantity = quantity }
            ?: ItemStockEntity(item = item, storageLocation = location, quantity = quantity)

        return itemStockRepository.save(stockEntity).toDomain()
    }

    /**
     * Moves stock from one location to another as a single transaction — decrementing
     * the source and crediting the destination never happen as two separate calls, so a
     * failure partway through can't leave stock double-counted or lost.
     */
    @Transactional
    fun moveStock(itemId: Long, fromLocationId: Long, toLocationId: Long, quantity: Int) {
        require(quantity > 0) { "Move quantity must be greater than 0." }
        require(fromLocationId != toLocationId) { "Source and destination locations must differ." }

        val source = itemStockRepository.findByItemIdAndStorageLocationId(itemId, fromLocationId)
            ?: throw InsufficientStockException(
                "Item $itemId has no stock at location $fromLocationId to move.",
            )

        if (source.quantity < quantity) {
            throw InsufficientStockException(
                "Cannot move $quantity units of item $itemId from location $fromLocationId: " +
                    "only ${source.quantity} unit(s) available there.",
            )
        }

        val destinationLocation = storageLocationRepository.findById(toLocationId)
            .orElseThrow { StorageLocationNotFoundException(toLocationId) }

        val remaining = source.quantity - quantity
        if (remaining == 0) {
            itemStockRepository.delete(source)
        } else {
            source.quantity = remaining
            itemStockRepository.save(source)
        }

        val destination = itemStockRepository.findByItemIdAndStorageLocationId(itemId, toLocationId)
        val destinationEntity = destination?.apply { this.quantity += quantity }
            ?: ItemStockEntity(item = source.item, storageLocation = destinationLocation, quantity = quantity)

        itemStockRepository.save(destinationEntity)
    }
}
