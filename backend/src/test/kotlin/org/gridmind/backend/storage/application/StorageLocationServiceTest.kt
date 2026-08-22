package org.gridmind.backend.storage.application

import org.gridmind.backend.shared.error.LocationInUseException
import org.gridmind.backend.shared.error.StorageLocationNotFoundException
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationEntity
import org.gridmind.backend.storage.infrastructure.persistence.StorageLocationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.dao.DataIntegrityViolationException
import java.util.Optional

class StorageLocationServiceTest {

    private val storageLocationRepository: StorageLocationRepository = mock(StorageLocationRepository::class.java)
    private val storageLocationService = StorageLocationService(storageLocationRepository)

    @Test
    fun `create succeeds without a parent`() {
        `when`(storageLocationRepository.save(any(StorageLocationEntity::class.java))).thenAnswer { invocation ->
            val entity = invocation.arguments[0] as StorageLocationEntity
            entity.id = 1L
            entity
        }

        val created = storageLocationService.create("Workshop", null)

        assertEquals("Workshop", created.name)
        assertEquals(1L, created.id)
    }

    @Test
    fun `create rejects an unknown parent`() {
        `when`(storageLocationRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<StorageLocationNotFoundException> { storageLocationService.create("Drawer 1", 99L) }
    }

    @Test
    fun `findChildren returns children of a location`() {
        `when`(storageLocationRepository.findByParentId(1L))
            .thenReturn(listOf(StorageLocationEntity(id = 2L, name = "Drawer 1")))

        val children = storageLocationService.findChildren(1L)

        assertEquals(1, children.size)
        assertEquals("Drawer 1", children[0].name)
    }

    @Test
    fun `delete removes an existing location with no children or stock`() {
        `when`(storageLocationRepository.existsById(1L)).thenReturn(true)

        storageLocationService.delete(1L)
    }

    @Test
    fun `delete throws StorageLocationNotFoundException for an unknown id`() {
        `when`(storageLocationRepository.existsById(99L)).thenReturn(false)

        assertThrows<StorageLocationNotFoundException> { storageLocationService.delete(99L) }
    }

    @Test
    fun `delete maps a constraint violation to LocationInUseException`() {
        `when`(storageLocationRepository.existsById(1L)).thenReturn(true)
        doThrow(DataIntegrityViolationException("still referenced"))
            .`when`(storageLocationRepository).deleteById(1L)

        assertThrows<LocationInUseException> { storageLocationService.delete(1L) }
    }
}
