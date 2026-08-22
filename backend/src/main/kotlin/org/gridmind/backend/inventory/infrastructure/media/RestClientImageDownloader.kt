package org.gridmind.backend.inventory.infrastructure.media

import org.gridmind.backend.inventory.application.DownloadedContent
import org.gridmind.backend.inventory.application.ImageDownloader
import org.slf4j.LoggerFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

// One byte over ImageStorageService's own size limit: reading up to this many bytes lets
// that service's own check reliably detect an oversized image without this class needing
// to know the exact limit itself.
private const val DOWNLOAD_CAP_BYTES = 5 * 1024 * 1024 + 1

/** The only place in the app that makes an outbound HTTP call to fetch a product image. */
@Component
class RestClientImageDownloader : ImageDownloader {
    private val logger = LoggerFactory.getLogger(RestClientImageDownloader::class.java)

    private val restClient = RestClient.builder()
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(5_000)
                setReadTimeout(5_000)
            },
        )
        .build()

    override fun download(url: String): DownloadedContent? = try {
        restClient.get().uri(url).exchange { _, response ->
            if (!response.statusCode.is2xxSuccessful()) {
                logger.warn("Image download from {} returned status {}", url, response.statusCode)
                return@exchange null
            }
            if (response.headers.contentLength > DOWNLOAD_CAP_BYTES) {
                logger.warn("Image at {} declares a content-length above the download cap.", url)
                return@exchange null
            }

            val bytes = response.body.readNBytes(DOWNLOAD_CAP_BYTES)
            val contentType = response.headers.contentType?.toString() ?: "application/octet-stream"
            DownloadedContent(bytes = bytes, contentType = contentType)
        }
    } catch (ex: Exception) {
        logger.warn("Failed to download image from {}: {}", url, ex.message)
        null
    }
}
