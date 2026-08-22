package org.gridmind.backend.storage.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface StorageLocationRepository : JpaRepository<StorageLocationEntity, Long> {
    fun findByParentId(parentId: Long?): List<StorageLocationEntity>
}
