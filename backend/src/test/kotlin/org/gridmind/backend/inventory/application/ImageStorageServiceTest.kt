package org.gridmind.backend.inventory.application

import org.gridmind.backend.inventory.infrastructure.persistence.StoredImageEntity
import org.gridmind.backend.inventory.infrastructure.persistence.StoredImageRepository
import org.gridmind.backend.shared.error.StoredImageNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Optional

class ImageStorageServiceTest {

    private val storedImageRepository: StoredImageRepository = mock(StoredImageRepository::class.java)

    private fun service(downloader: ImageDownloader): ImageStorageService {
        val tempDir = Files.createTempDirectory("gridmind-media-test")
        return ImageStorageService(storedImageRepository, downloader, tempDir.toString())
    }

    @Test
    fun `returns null when the download fails`() {
        val service = service(ImageDownloader { null })

        val result = service.downloadAndStore("https://example.com/a.png", "DigiKey")

        assertNull(result)
        verify(storedImageRepository, never()).save(any())
    }

    @Test
    fun `returns null without throwing when the downloader throws`() {
        val service = service(ImageDownloader { throw RuntimeException("boom") })

        val result = service.downloadAndStore("https://example.com/a.png", "DigiKey")

        assertNull(result)
    }

    @Test
    fun `returns null when the image exceeds the size limit`() {
        val oversized = ByteArray(6 * 1024 * 1024)
        val service = service(ImageDownloader { DownloadedContent(oversized, "image/png") })

        val result = service.downloadAndStore("https://example.com/a.png", "DigiKey")

        assertNull(result)
        verify(storedImageRepository, never()).save(any())
    }

    @Test
    fun `downloads, writes the file and persists metadata`() {
        val bytes = "fake-image-bytes".toByteArray()
        val checksum = sha256Hex(bytes)
        val tempDir = Files.createTempDirectory("gridmind-media-test")
        `when`(storedImageRepository.findByChecksum(checksum)).thenReturn(null)
        `when`(storedImageRepository.save(any())).thenAnswer { it.arguments[0] as StoredImageEntity }

        val service = ImageStorageService(storedImageRepository, ImageDownloader { DownloadedContent(bytes, "image/png") }, tempDir.toString())
        val result = service.downloadAndStore("https://example.com/a.png", "DigiKey")

        assertEquals(checksum, result?.checksum)
        assertEquals("DigiKey", result?.sourceProvider)
        assertEquals("https://example.com/a.png", result?.sourceUrl)
        assertEquals(true, result?.filePath?.let { Files.exists(Path.of(it)) })
    }

    @Test
    fun `deduplicates by checksum instead of writing a second file`() {
        val bytes = "same-bytes".toByteArray()
        val checksum = sha256Hex(bytes)
        val existing = StoredImageEntity(
            id = 7L,
            checksum = checksum,
            contentType = "image/png",
            filePath = "/data/media/$checksum.png",
        )
        `when`(storedImageRepository.findByChecksum(checksum)).thenReturn(existing)

        val service = service(ImageDownloader { DownloadedContent(bytes, "image/png") })
        val result = service.downloadAndStore("https://example.com/a.png", "Mouser")

        assertEquals(7L, result?.id)
        verify(storedImageRepository, never()).save(any())
    }

    @Test
    fun `findById throws for an unknown id`() {
        `when`(storedImageRepository.findById(99L)).thenReturn(Optional.empty())
        val service = service(ImageDownloader { null })

        assertThrows<StoredImageNotFoundException> { service.findById(99L) }
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
