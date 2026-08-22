package org.gridmind.backend.inventory.infrastructure.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class StoredImageRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var storedImageRepository: StoredImageRepository

    @Test
    fun `findByChecksum finds an existing image`() {
        entityManager.persistAndFlush(
            StoredImageEntity(
                checksum = "abc123",
                contentType = "image/png",
                filePath = "/data/media/abc123.png",
                sourceProvider = "DigiKey",
                sourceUrl = "https://example.com/a.png",
            ),
        )

        val found = storedImageRepository.findByChecksum("abc123")

        assertEquals("/data/media/abc123.png", found?.filePath)
        assertEquals("DigiKey", found?.sourceProvider)
    }

    @Test
    fun `findByChecksum returns null for an unknown checksum`() {
        assertNull(storedImageRepository.findByChecksum("does-not-exist"))
    }

    @Test
    fun `checksum is unique`() {
        entityManager.persistAndFlush(
            StoredImageEntity(checksum = "dup", contentType = "image/png", filePath = "/data/media/1.png"),
        )

        assertThrows<Exception> {
            entityManager.persistAndFlush(
                StoredImageEntity(checksum = "dup", contentType = "image/png", filePath = "/data/media/2.png"),
            )
        }
    }
}
