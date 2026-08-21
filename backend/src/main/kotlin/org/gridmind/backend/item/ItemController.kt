package org.gridmind.backend.item

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:5173"])
class ItemController {

    private val items = listOf(
        Item(1, "ESP32-S3", 2),
        Item(2, "Matrice HUB75", 1),
        Item(3, "Arduino Nano", 4),
    )

    @GetMapping("/api/items")
    fun getItems(): List<Item> = items
}
