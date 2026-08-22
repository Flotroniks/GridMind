package org.gridmind.backend.category.application

import org.gridmind.backend.category.domain.Category
import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.category.infrastructure.persistence.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Use cases for the data-driven category list — just listing and creating, since categories are never renamed or removed once items reference them. */
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
}
