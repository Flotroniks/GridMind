package org.gridmind.backend.category.application

import org.gridmind.backend.category.domain.Category
import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.category.infrastructure.persistence.CategoryRepository
import org.gridmind.backend.shared.error.CategoryNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Use cases for the data-driven category list — listing, creating, renaming, and
 * deleting. Deleting is safe by design: `inventory_items.category_id` is
 * `ON DELETE SET NULL` (see the V3 migration), so a deleted category's items simply
 * become uncategorized rather than being blocked or cascaded away. */
@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    fun findAll(): List<Category> =
        categoryRepository.findAll().map(CategoryEntity::toDomain)

    @Transactional
    fun create(name: String): Category {
        val category = Category(name = name)
        require(!categoryRepository.existsByNameIgnoreCase(category.name.trim())) {
            "Category '${category.name.trim()}' already exists."
        }
        return categoryRepository.save(CategoryEntity.fromDomain(category)).toDomain()
    }

    @Transactional
    fun rename(id: Long, name: String): Category {
        val entity = categoryRepository.findById(id).orElseThrow { CategoryNotFoundException(id) }
        val trimmed = name.trim()
        if (!trimmed.equals(entity.name, ignoreCase = true)) {
            require(!categoryRepository.existsByNameIgnoreCase(trimmed)) {
                "Category '$trimmed' already exists."
            }
        }
        entity.name = trimmed
        return categoryRepository.save(entity).toDomain()
    }

    @Transactional
    fun delete(id: Long) {
        if (!categoryRepository.existsById(id)) {
            throw CategoryNotFoundException(id)
        }
        categoryRepository.deleteById(id)
    }
}
