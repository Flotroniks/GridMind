package org.gridmind.backend.inventory.application

import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.category.infrastructure.persistence.CategoryRepository
import org.gridmind.backend.inventory.domain.Item
import org.gridmind.backend.inventory.domain.ItemStatus
import org.gridmind.backend.inventory.infrastructure.persistence.ItemEntity
import org.gridmind.backend.inventory.infrastructure.persistence.ItemRepository
import org.gridmind.backend.inventory.infrastructure.persistence.ItemSpecifications
import org.gridmind.backend.shared.error.ItemNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Use cases for managing inventory items — listing/searching, and the CRUD lifecycle. */
@Service
class InventoryService(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
) {
    fun findAll(): List<Item> = search(term = null, categoryId = null, manufacturer = null, status = null)

    @Transactional(readOnly = true)
    fun search(term: String?, categoryId: Long?, manufacturer: String?, status: ItemStatus? = null): List<Item> =
        itemRepository.findAll(ItemSpecifications.buildSpec(term, categoryId, manufacturer, status))
            .map(ItemEntity::toDomain)

    @Transactional(readOnly = true)
    fun findById(id: Long): Item =
        itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException(id) }
            .toDomain()

    @Transactional
    fun create(item: Item): Item {
        val category = resolveCategory(item.categoryId)
        return itemRepository.save(ItemEntity.fromDomain(item, category)).toDomain()
    }

    @Transactional
    fun update(id: Long, item: Item): Item {
        val entity = itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException(id) }
        val category = resolveCategory(item.categoryId)

        entity.name = item.name.trim()
        entity.quantity = item.quantity
        entity.description = item.description?.trim()
        entity.manufacturer = item.manufacturer?.trim()
        entity.reference = item.reference?.trim()
        entity.category = category
        entity.tags = item.tags.toMutableList()
        entity.notes = item.notes?.trim()
        entity.productUrl = item.productUrl?.trim()
        entity.datasheetUrl = item.datasheetUrl?.trim()
        entity.minimumQuantity = item.minimumQuantity
        entity.status = item.status

        return itemRepository.save(entity).toDomain()
    }

    @Transactional
    fun delete(id: Long) {
        if (!itemRepository.existsById(id)) {
            throw ItemNotFoundException(id)
        }
        itemRepository.deleteById(id)
    }

    private fun resolveCategory(categoryId: Long?): CategoryEntity? {
        if (categoryId == null) return null
        return categoryRepository.findById(categoryId)
            .orElseThrow { IllegalArgumentException("Category with id $categoryId was not found.") }
    }
}
