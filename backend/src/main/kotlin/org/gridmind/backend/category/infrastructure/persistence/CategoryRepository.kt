package org.gridmind.backend.category.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<CategoryEntity, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
}
