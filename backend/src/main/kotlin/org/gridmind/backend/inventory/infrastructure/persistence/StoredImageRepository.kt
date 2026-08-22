package org.gridmind.backend.inventory.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface StoredImageRepository : JpaRepository<StoredImageEntity, Long> {
    fun findByChecksum(checksum: String): StoredImageEntity?
}
