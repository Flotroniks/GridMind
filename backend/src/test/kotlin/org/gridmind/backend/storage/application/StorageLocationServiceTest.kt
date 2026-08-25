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
    fun `rename updates the name and leaves the id and parent untouched`() {
        val entity = StorageLocationEntity(id = 2L, name = "Drawer 1")
        `when`(storageLocationRepository.findById(2L)).thenReturn(Optional.of(entity))
        `when`(storageLocationRepository.save(entity)).thenReturn(entity)

        val renamed = storageLocationService.rename(2L, "Capacitors")

        assertEquals(2L, renamed.id)
        assertEquals("Capacitors", renamed.name)
    }

    @Test
    fun `rename trims the new name`() {
        val entity = StorageLocationEntity(id = 2L, name = "Drawer 1")
        `when`(storageLocationRepository.findById(2L)).thenReturn(Optional.of(entity))
        `when`(storageLocationRepository.save(entity)).thenReturn(entity)

        val renamed = storageLocationService.rename(2L, "  Capacitors  ")

        assertEquals("Capacitors", renamed.name)
    }

    @Test
    fun `rename rejects a blank name`() {
        assertThrows<IllegalArgumentException> { storageLocationService.rename(2L, "   ") }
    }

    @Test
    fun `rename throws StorageLocationNotFoundException for an unknown id`() {
        `when`(storageLocationRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<StorageLocationNotFoundException> { storageLocationService.rename(99L, "Capacitors") }
    }

    @Test
    fun `configureLed sets the controller id and index`() {
        val entity = StorageLocationEntity(id = 2L, name = "Drawer 1")
        `when`(storageLocationRepository.findById(2L)).thenReturn(Optional.of(entity))
        `when`(storageLocationRepository.save(entity)).thenReturn(entity)

        val configured = storageLocationService.configureLed(2L, "strip-a", 3)

        assertEquals("strip-a", configured.ledControllerId)
        assertEquals(3, configured.ledIndex)
    }

    @Test
    fun `configureLed clears the controller id and index when both are null`() {
        val entity = StorageLocationEntity(id = 2L, name = "Drawer 1", ledControllerId = "strip-a", ledIndex = 3)
        `when`(storageLocationRepository.findById(2L)).thenReturn(Optional.of(entity))
        `when`(storageLocationRepository.save(entity)).thenReturn(entity)

        val configured = storageLocationService.configureLed(2L, null, null)

        assertEquals(null, configured.ledControllerId)
        assertEquals(null, configured.ledIndex)
    }

    @Test
    fun `configureLed rejects a controller id without an index`() {
        assertThrows<IllegalArgumentException> { storageLocationService.configureLed(2L, "strip-a", null) }
    }

    @Test
    fun `configureLed rejects a negative index`() {
        assertThrows<IllegalArgumentException> { storageLocationService.configureLed(2L, "strip-a", -1) }
    }

    @Test
    fun `configureLed throws StorageLocationNotFoundException for an unknown id`() {
        `when`(storageLocationRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<StorageLocationNotFoundException> { storageLocationService.configureLed(99L, "strip-a", 0) }
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
