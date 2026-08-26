package org.gridmind.backend.storage.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StorageLocationRepository : JpaRepository<StorageLocationEntity, Long> {
    fun findByParentId(parentId: Long?): List<StorageLocationEntity>

    /** Ids of locations no other location currently points at as a parent — i.e. it's
     * safe to delete them without hitting the self-referential `ON DELETE RESTRICT` on
     * `parent_id`. Used to wipe the whole hierarchy leaf-first, one layer at a time; see
     * [org.gridmind.backend.backup.application.BackupService]. */
    @Query(
        "select l.id from StorageLocationEntity l where l.id not in " +
            "(select p.parent.id from StorageLocationEntity p where p.parent is not null)",
    )
    fun findLeafIds(): List<Long>
}
