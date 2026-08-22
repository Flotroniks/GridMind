package org.gridmind.backend.inventory.application

import org.gridmind.backend.inventory.domain.StoredImage
import org.gridmind.backend.inventory.infrastructure.persistence.StoredImageEntity
import org.gridmind.backend.inventory.infrastructure.persistence.StoredImageRepository
import org.gridmind.backend.shared.error.StoredImageNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/** Raw bytes downloaded from a provider, with the content type the server reported for them. */
data class DownloadedContent(val bytes: ByteArray, val contentType: String)

/**
 * Fetches a product image from wherever it actually lives. Kept as its own tiny seam so
 * [ImageStorageService] can be tested without a real HTTP call — see
 * [org.gridmind.backend.inventory.infrastructure.media.RestClientImageDownloader] for the
 * implementation that's actually wired up.
 */
fun interface ImageDownloader {
    /** Returns `null` if the URL couldn't be fetched at all (bad status, unreachable, ...). */
    fun download(url: String): DownloadedContent?
}

private const val MAX_IMAGE_BYTES = 5 * 1024 * 1024L

/**
 * Turns an external image URL into a local file GridMind owns. Every call is best-effort:
 * a provider going down, a broken link, or an oversized file never bubbles up as an
 * exception — it just means no image gets attached, which the caller treats the same as
 * the user never having provided one.
 *
 * Downloads are deduplicated by content: the same picture found through two different
 * searches (or two different providers) is only ever written to disk once, keyed by its
 * SHA-256 checksum.
 */
@Service
class ImageStorageService(
    private val storedImageRepository: StoredImageRepository,
    private val imageDownloader: ImageDownloader,
    @Value("\${gridmind.media.storage-path:./data/media}") storagePath: String,
) {
    private val logger = LoggerFactory.getLogger(ImageStorageService::class.java)
    private val storageDir: Path = Path.of(storagePath).also { Files.createDirectories(it) }

    @Transactional(readOnly = true)
    fun findById(id: Long): StoredImage =
        storedImageRepository.findById(id).orElseThrow { StoredImageNotFoundException(id) }.toDomain()

    @Transactional
    fun downloadAndStore(url: String, sourceProvider: String?): StoredImage? = try {
        val content = imageDownloader.download(url)
        when {
            content == null -> {
                logger.warn("Could not download image from {}", url)
                null
            }
            content.bytes.size > MAX_IMAGE_BYTES -> {
                logger.warn("Image at {} exceeds the {}-byte limit, skipping.", url, MAX_IMAGE_BYTES)
                null
            }
            else -> store(content, url, sourceProvider)
        }
    } catch (ex: Exception) {
        logger.warn("Failed to download and store image from {}: {}", url, ex.message)
        null
    }

    private fun store(content: DownloadedContent, sourceUrl: String, sourceProvider: String?): StoredImage {
        val checksum = sha256Hex(content.bytes)

        storedImageRepository.findByChecksum(checksum)?.let { return it.toDomain() }

        val filePath = storageDir.resolve(checksum + extensionFor(content.contentType))
        Files.write(filePath, content.bytes)

        return storedImageRepository.save(
            StoredImageEntity(
                checksum = checksum,
                contentType = content.contentType,
                filePath = filePath.toString(),
                sourceProvider = sourceProvider,
                sourceUrl = sourceUrl,
            ),
        ).toDomain()
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun extensionFor(contentType: String): String = when (contentType.substringBefore(';').trim()) {
        "image/jpeg", "image/jpg" -> ".jpg"
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        "image/gif" -> ".gif"
        else -> ""
    }
}
