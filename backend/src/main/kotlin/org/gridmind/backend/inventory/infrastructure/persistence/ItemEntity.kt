package org.gridmind.backend.inventory.infrastructure.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.inventory.domain.Item

@Entity
@Table(name = "inventory_items")
class ItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 120)
    var name: String,

    @Column(nullable = false)
    var quantity: Int,

    @Column
    var description: String? = null,

    @Column(length = 120)
    var manufacturer: String? = null,

    @Column(length = 120)
    var reference: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: CategoryEntity? = null,

    @ElementCollection
    @CollectionTable(name = "item_tags", joinColumns = [JoinColumn(name = "item_id")])
    @Column(name = "tag", length = 40)
    var tags: MutableList<String> = mutableListOf(),

    @Column
    var notes: String? = null,

    @Column(name = "product_url", length = 500)
    var productUrl: String? = null,

    @Column(name = "datasheet_url", length = 500)
    var datasheetUrl: String? = null,

    @Column(name = "minimum_quantity", nullable = false)
    var minimumQuantity: Int = 0,
) {
    fun toDomain(): Item = Item(
        id = id,
        name = name,
        quantity = quantity,
        description = description,
        manufacturer = manufacturer,
        reference = reference,
        categoryId = category?.id,
        tags = tags.toList(),
        notes = notes,
        productUrl = productUrl,
        datasheetUrl = datasheetUrl,
        minimumQuantity = minimumQuantity,
    )

    companion object {
        fun fromDomain(item: Item, category: CategoryEntity?): ItemEntity = ItemEntity(
            id = item.id,
            name = item.name.trim(),
            quantity = item.quantity,
            description = item.description?.trim(),
            manufacturer = item.manufacturer?.trim(),
            reference = item.reference?.trim(),
            category = category,
            tags = item.tags.toMutableList(),
            notes = item.notes?.trim(),
            productUrl = item.productUrl?.trim(),
            datasheetUrl = item.datasheetUrl?.trim(),
            minimumQuantity = item.minimumQuantity,
        )
    }
}
