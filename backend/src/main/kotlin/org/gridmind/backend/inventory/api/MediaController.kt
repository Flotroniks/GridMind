package org.gridmind.backend.inventory.api

import org.gridmind.backend.inventory.application.ImageStorageService
import org.gridmind.backend.shared.error.StoredImageNotFoundException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Path

@RestController
@RequestMapping("/api/media")
class MediaController(
    private val imageStorageService: ImageStorageService,
) {
    @GetMapping("/{id}")
    fun getMedia(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val image = imageStorageService.findById(id)
        val path = Path.of(image.filePath)
        if (!Files.exists(path)) {
            throw StoredImageNotFoundException(id)
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.contentType))
            .body(Files.readAllBytes(path))
    }
}
