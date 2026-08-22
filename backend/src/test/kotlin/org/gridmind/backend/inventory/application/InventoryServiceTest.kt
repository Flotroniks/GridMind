package org.gridmind.backend.inventory.application

import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.category.infrastructure.persistence.CategoryRepository
import org.gridmind.backend.inventory.domain.Item
import org.gridmind.backend.inventory.infrastructure.persistence.ItemEntity
import org.gridmind.backend.inventory.infrastructure.persistence.ItemRepository
import org.gridmind.backend.shared.error.ItemNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional

class InventoryServiceTest {

    private val itemRepository: ItemRepository = mock(ItemRepository::class.java)
    private val categoryRepository: CategoryRepository = mock(CategoryRepository::class.java)
    private val inventoryService = InventoryService(itemRepository, categoryRepository)

    @Test
    fun `service creates an item with valid data`() {
        `when`(itemRepository.save(any(ItemEntity::class.java))).thenAnswer { invocation ->
            val entity = invocation.arguments[0] as ItemEntity
            entity.id = 10L
            entity
        }

        val created = inventoryService.create(Item(name = "ESP32-S3", quantity = 4))

        assertEquals("ESP32-S3", created.name)
        assertEquals(4, created.quantity)
        assertEquals(10L, created.id)
    }

    @Test
    fun `service rejects negative quantities`() {
        val exception = assertThrows<IllegalArgumentException> {
            Item(name = "Bad part", quantity = -1)
        }

        assertEquals("Item quantity must be greater than or equal to 0.", exception.message)
    }

    @Test
    fun `create resolves the given category`() {
        `when`(categoryRepository.findById(3L))
            .thenReturn(Optional.of(CategoryEntity(id = 3L, name = "Sensors")))
        `when`(itemRepository.save(any(ItemEntity::class.java))).thenAnswer { invocation ->
            invocation.arguments[0] as ItemEntity
        }

        val created = inventoryService.create(Item(name = "PIR sensor", quantity = 1, categoryId = 3L))

        assertEquals(3L, created.categoryId)
    }

    @Test
    fun `create rejects an unknown category`() {
        `when`(categoryRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<IllegalArgumentException> {
            inventoryService.create(Item(name = "Widget", quantity = 1, categoryId = 99L))
        }
    }

    @Test
    fun `findById returns the matching item`() {
        `when`(itemRepository.findById(7L)).thenReturn(Optional.of(ItemEntity(id = 7L, name = "Wire", quantity = 2)))

        val found = inventoryService.findById(7L)

        assertEquals("Wire", found.name)
        assertEquals(2, found.quantity)
    }

    @Test
    fun `findById throws ItemNotFoundException for an unknown id`() {
        `when`(itemRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ItemNotFoundException> { inventoryService.findById(99L) }
    }

    @Test
    fun `update replaces name and quantity`() {
        val existing = ItemEntity(id = 7L, name = "Wire", quantity = 2)

        `when`(itemRepository.findById(7L)).thenReturn(Optional.of(existing))
        `when`(itemRepository.save(any(ItemEntity::class.java))).thenAnswer { invocation ->
            invocation.arguments[0] as ItemEntity
        }

        val updated = inventoryService.update(7L, Item(name = "Copper wire", quantity = 8))

        assertEquals("Copper wire", updated.name)
        assertEquals(8, updated.quantity)
        assertEquals("Copper wire", existing.name)
        assertEquals(8, existing.quantity)
    }

    @Test
    fun `update throws ItemNotFoundException for an unknown id`() {
        `when`(itemRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ItemNotFoundException> { inventoryService.update(99L, Item(name = "Anything", quantity = 1)) }
    }

    @Test
    fun `delete removes an existing item`() {
        `when`(itemRepository.existsById(7L)).thenReturn(true)

        inventoryService.delete(7L)

        verify(itemRepository, times(1)).deleteById(7L)
    }

    @Test
    fun `delete throws ItemNotFoundException for an unknown id`() {
        `when`(itemRepository.existsById(99L)).thenReturn(false)

        assertThrows<ItemNotFoundException> { inventoryService.delete(99L) }
    }
}
