package org.gridmind.backend.storage.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.gridmind.backend.storage.domain.StorageLocation

@Entity
@Table(name = "storage_locations")
class StorageLocationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 120)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: StorageLocationEntity? = null,

    @Column(name = "led_controller_id", length = 60)
    var ledControllerId: String? = null,

    @Column(name = "led_index")
    var ledIndex: Int? = null,
) {
    fun toDomain(): StorageLocation = StorageLocation(
        id = id,
        name = name,
        parentId = parent?.id,
        ledControllerId = ledControllerId,
        ledIndex = ledIndex,
    )

    companion object {
        fun fromDomain(location: StorageLocation, parent: StorageLocationEntity?): StorageLocationEntity =
            StorageLocationEntity(
                id = location.id,
                name = location.name.trim(),
                parent = parent,
                ledControllerId = location.ledControllerId,
                ledIndex = location.ledIndex,
            )
    }
}
