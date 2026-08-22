package org.gridmind.backend.storage.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.gridmind.backend.inventory.infrastructure.persistence.ItemEntity
import org.gridmind.backend.storage.domain.ItemStock

@Entity
@Table(
    name = "item_stock",
    uniqueConstraints = [UniqueConstraint(columnNames = ["item_id", "storage_location_id"])],
)
class ItemStockEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    var item: ItemEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_location_id", nullable = false)
    var storageLocation: StorageLocationEntity,

    @Column(nullable = false)
    var quantity: Int,
) {
    fun toDomain(): ItemStock = ItemStock(
        id = id,
        itemId = item.id!!,
        storageLocationId = storageLocation.id!!,
        quantity = quantity,
    )
}
