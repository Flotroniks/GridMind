package org.gridmind.backend.storage.application

import org.gridmind.backend.shared.error.LocationInUseException
import org.gridmind.backend.shared.error.StorageLocationNotFoundException
import org.gridmind.backend.storage.domain.StorageLocation
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationEntity
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Use cases for browsing and managing the storage location hierarchy (not the stock kept in it — see [StockAllocationService]). */
@Service
class StorageLocationService(
    private val storageLocationRepository: StorageLocationRepository,
) {
    @Transactional(readOnly = true)
    fun findChildren(parentId: Long?): List<StorageLocation> =
        storageLocationRepository.findByParentId(parentId).map(StorageLocationEntity::toDomain)

    @Transactional(readOnly = true)
    fun findById(id: Long): StorageLocation =
        storageLocationRepository.findById(id)
            .orElseThrow { StorageLocationNotFoundException(id) }
            .toDomain()

    @Transactional
    fun create(name: String, parentId: Long?): StorageLocation {
        val parent = parentId?.let {
            storageLocationRepository.findById(it)
                .orElseThrow { StorageLocationNotFoundException(it) }
        }
        val location = StorageLocation(name = name, parentId = parentId)
        return storageLocationRepository.save(StorageLocationEntity.fromDomain(location, parent)).toDomain()
    }

    /**
     * Deletes a location. Fails loudly instead of cascading: a location that still has
     * child locations or stock in it must be emptied out first — the FK constraints
     * enforce this at the database level, and we just translate the violation into a
     * clearer [LocationInUseException] here.
     */
    @Transactional
    fun delete(id: Long) {
        if (!storageLocationRepository.existsById(id)) {
            throw StorageLocationNotFoundException(id)
        }
        try {
            storageLocationRepository.deleteById(id)
            storageLocationRepository.flush()
        } catch (ex: DataIntegrityViolationException) {
            throw LocationInUseException(
                "Storage location with id $id still has child locations or stock and cannot be deleted.",
            )
        }
    }
}
