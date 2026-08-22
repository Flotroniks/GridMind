package org.gridmind.backend.inventory.infrastructure.persistence

import org.springframework.data.jpa.domain.Specification

/**
 * Builds the WHERE clause for item search/filtering one predicate at a time.
 *
 * Each `hasX` returns `null` when its filter wasn't provided, so [buildSpec] can just
 * drop the nulls and AND together whatever's left — that avoids writing a derived
 * query method for every combination of search/category/manufacturer.
 */
object ItemSpecifications {

    fun hasNameLike(term: String?): Specification<ItemEntity>? {
        if (term.isNullOrBlank()) return null
        return Specification { root, _, cb ->
            cb.like(cb.lower(root.get("name")), "%${term.trim().lowercase()}%")
        }
    }

    fun hasCategoryId(categoryId: Long?): Specification<ItemEntity>? {
        if (categoryId == null) return null
        return Specification { root, _, cb ->
            cb.equal(root.get<Any>("category").get<Any>("id"), categoryId)
        }
    }

    fun hasManufacturerLike(manufacturer: String?): Specification<ItemEntity>? {
        if (manufacturer.isNullOrBlank()) return null
        return Specification { root, _, cb ->
            cb.like(cb.lower(root.get("manufacturer")), "%${manufacturer.trim().lowercase()}%")
        }
    }

    fun buildSpec(term: String?, categoryId: Long?, manufacturer: String?): Specification<ItemEntity> =
        Specification.allOf(
            listOfNotNull(
                hasNameLike(term),
                hasCategoryId(categoryId),
                hasManufacturerLike(manufacturer),
            ),
        )
}
