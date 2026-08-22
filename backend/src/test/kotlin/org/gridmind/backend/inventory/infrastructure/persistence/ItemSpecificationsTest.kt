package org.gridmind.backend.inventory.infrastructure.persistence

import org.gridmind.backend.category.infrastructure.persistence.CategoryEntity
import org.gridmind.backend.inventory.domain.ItemStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class ItemSpecificationsTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @Test
    fun `buildSpec with no filters returns every item`() {
        entityManager.persistAndFlush(ItemEntity(name = "ESP32-S3", quantity = 1))
        entityManager.persistAndFlush(ItemEntity(name = "Wire", quantity = 5))

        val result = itemRepository.findAll(ItemSpecifications.buildSpec(null, null, null))

        assertEquals(2, result.size)
    }

    @Test
    fun `buildSpec filters by name term case-insensitively`() {
        entityManager.persistAndFlush(ItemEntity(name = "ESP32-S3 DevKitC", quantity = 1))
        entityManager.persistAndFlush(ItemEntity(name = "Wire", quantity = 5))

        val result = itemRepository.findAll(ItemSpecifications.buildSpec("esp32", null, null))

        assertEquals(1, result.size)
        assertEquals("ESP32-S3 DevKitC", result[0].name)
    }

    @Test
    fun `buildSpec filters by category id`() {
        val sensors = entityManager.persistAndFlush(CategoryEntity(name = "Sensors"))
        val modules = entityManager.persistAndFlush(CategoryEntity(name = "Modules"))
        entityManager.persistAndFlush(ItemEntity(name = "PIR", quantity = 1, category = sensors))
        entityManager.persistAndFlush(ItemEntity(name = "HUB75", quantity = 1, category = modules))

        val result = itemRepository.findAll(ItemSpecifications.buildSpec(null, sensors.id, null))

        assertEquals(1, result.size)
        assertEquals("PIR", result[0].name)
    }

    @Test
    fun `buildSpec filters by manufacturer case-insensitively`() {
        entityManager.persistAndFlush(ItemEntity(name = "ESP32-S3", quantity = 1, manufacturer = "Espressif"))
        entityManager.persistAndFlush(ItemEntity(name = "Wire", quantity = 5, manufacturer = "Generic"))

        val result = itemRepository.findAll(ItemSpecifications.buildSpec(null, null, "espressif"))

        assertEquals(1, result.size)
        assertEquals("ESP32-S3", result[0].name)
    }

    @Test
    fun `buildSpec filters by status`() {
        entityManager.persistAndFlush(ItemEntity(name = "ESP32-S3", quantity = 1, status = ItemStatus.IN_SERVICE))
        entityManager.persistAndFlush(ItemEntity(name = "Broken fan", quantity = 1, status = ItemStatus.OUT_OF_SERVICE))

        val result = itemRepository.findAll(ItemSpecifications.buildSpec(null, null, null, ItemStatus.OUT_OF_SERVICE))

        assertEquals(1, result.size)
        assertEquals("Broken fan", result[0].name)
    }

    @Test
    fun `buildSpec combines filters`() {
        val modules = entityManager.persistAndFlush(CategoryEntity(name = "Modules"))
        entityManager.persistAndFlush(
            ItemEntity(name = "ESP32-S3", quantity = 1, manufacturer = "Espressif", category = modules),
        )
        entityManager.persistAndFlush(ItemEntity(name = "ESP32-C3", quantity = 1, manufacturer = "Other", category = modules))

        val result = itemRepository.findAll(ItemSpecifications.buildSpec("esp32", modules.id, "Espressif"))

        assertEquals(1, result.size)
        assertEquals("ESP32-S3", result[0].name)
    }
}
