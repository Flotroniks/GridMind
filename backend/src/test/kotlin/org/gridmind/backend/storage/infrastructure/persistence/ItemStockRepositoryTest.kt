package org.gridmind.backend.storage.infrastructure.persistence

import org.gridmind.backend.inventory.infrastructure.persistence.ItemEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class ItemStockRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var itemStockRepository: ItemStockRepository

    @Test
    fun `sumQuantityByItemId adds up quantity across locations`() {
        val item = entityManager.persistAndFlush(ItemEntity(name = "ESP32-S3", quantity = 10))
        val locationA = entityManager.persistAndFlush(StorageLocationEntity(name = "Drawer A"))
        val locationB = entityManager.persistAndFlush(StorageLocationEntity(name = "Drawer B"))
        entityManager.persistAndFlush(ItemStockEntity(item = item, storageLocation = locationA, quantity = 3))
        entityManager.persistAndFlush(ItemStockEntity(item = item, storageLocation = locationB, quantity = 4))

        assertEquals(7, itemStockRepository.sumQuantityByItemId(item.id!!))
    }

    @Test
    fun `sumQuantityByItemId returns zero when no stock exists`() {
        val item = entityManager.persistAndFlush(ItemEntity(name = "Wire", quantity = 10))

        assertEquals(0, itemStockRepository.sumQuantityByItemId(item.id!!))
    }

    @Test
    fun `item and location pair must be unique`() {
        val item = entityManager.persistAndFlush(ItemEntity(name = "ESP32-S3", quantity = 10))
        val location = entityManager.persistAndFlush(StorageLocationEntity(name = "Drawer A"))
        entityManager.persistAndFlush(ItemStockEntity(item = item, storageLocation = location, quantity = 3))

        assertThrows<DataIntegrityViolationException> {
            itemStockRepository.saveAndFlush(ItemStockEntity(item = item, storageLocation = location, quantity = 1))
        }
    }
}
