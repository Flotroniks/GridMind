package org.gridmind.backend.storage.infrastructure.persistence

import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

interface ItemStockRepository : JpaRepository<ItemStockEntity, Long> {
    fun findByItemId(itemId: Long): List<ItemStockEntity>
    fun findByStorageLocationId(storageLocationId: Long): List<ItemStockEntity>
    fun findByItemIdAndStorageLocationId(itemId: Long, storageLocationId: Long): ItemStockEntity?

    @Query("select coalesce(sum(s.quantity), 0) from ItemStockEntity s where s.item.id = :itemId")
    fun sumQuantityByItemId(@Param("itemId") itemId: Long): Int
}
