package org.gridmind.backend.inventory.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.gridmind.backend.inventory.domain.StoredImage
import java.time.Instant

@Entity
@Table(name = "stored_images")
class StoredImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 64)
    var checksum: String,

    @Column(name = "content_type", nullable = false, length = 100)
    var contentType: String,

    @Column(name = "file_path", nullable = false, length = 500)
    var filePath: String,

    @Column(name = "source_provider", length = 120)
    var sourceProvider: String? = null,

    @Column(name = "source_url", length = 500)
    var sourceUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
) {
    fun toDomain(): StoredImage = StoredImage(
        id = id,
        checksum = checksum,
        contentType = contentType,
        filePath = filePath,
        sourceProvider = sourceProvider,
        sourceUrl = sourceUrl,
    )

    companion object {
        fun fromDomain(image: StoredImage): StoredImageEntity = StoredImageEntity(
            id = image.id,
            checksum = image.checksum,
            contentType = image.contentType,
            filePath = image.filePath,
            sourceProvider = image.sourceProvider,
            sourceUrl = image.sourceUrl,
        )
    }
}
