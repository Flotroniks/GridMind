package org.gridmind.backend.imageanalysis.application

import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Sniffs the real bytes of an uploaded image rather than trusting its filename or the
 * browser-supplied Content-Type — both are trivial to spoof and neither is checked here.
 * Only JPEG and PNG are accepted: WEBP was in scope conceptually, but the JDK's built-in
 * ImageIO has no WEBP reader, and pulling in a plugin just for this prototype isn't worth
 * it — see the README's "Local AI / Image analysis" section.
 */
object UploadedImageValidator {

    private const val MAX_SIZE_BYTES = 10 * 1024 * 1024
    private val ALLOWED_FORMATS = setOf("JPEG", "PNG")

    fun validate(bytes: ByteArray) {
        require(bytes.isNotEmpty()) { "The uploaded file is empty." }
        require(bytes.size <= MAX_SIZE_BYTES) {
            "The uploaded file exceeds the maximum allowed size (${MAX_SIZE_BYTES / (1024 * 1024)} MiB)."
        }

        val format = detectFormat(bytes)
        require(format != null && format in ALLOWED_FORMATS) {
            "Unsupported image format. Accepted formats: JPEG, PNG."
        }

        val decodable = try {
            ImageIO.read(ByteArrayInputStream(bytes)) != null
        } catch (_: IOException) {
            false
        }
        require(decodable) { "The file is not a valid image, or it is corrupted." }
    }

    /** Asks ImageIO which format (if any) actually matches the byte content — not the extension. */
    private fun detectFormat(bytes: ByteArray): String? {
        val stream = ImageIO.createImageInputStream(ByteArrayInputStream(bytes)) ?: return null
        return stream.use {
            val readers = ImageIO.getImageReaders(it)
            if (readers.hasNext()) readers.next().formatName.uppercase() else null
        }
    }
}
