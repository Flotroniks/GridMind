package org.gridmind.backend.inventory.api

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.gridmind.backend.category.application.CategoryService
import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.inventory.domain.Item
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/inventory")
class ItemController(
    private val inventoryService: InventoryService,
    private val categoryService: CategoryService,
) {
    @GetMapping("/items")
    fun listItems(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) manufacturer: String?,
    ): List<ItemResponse> {
        val categoryNames = categoryNamesById()
        return inventoryService.search(search, categoryId, manufacturer)
            .map { ItemResponse.from(it, categoryNames[it.categoryId]) }
    }

    @GetMapping("/items/{id}")
    fun getItem(@PathVariable id: Long): ItemResponse {
        val item = inventoryService.findById(id)
        return ItemResponse.from(item, categoryNamesById()[item.categoryId])
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun createItem(@Valid @RequestBody request: ItemRequest): ItemResponse {
        val item = inventoryService.create(request.toItem())
        return ItemResponse.from(item, categoryNamesById()[item.categoryId])
    }

    @PatchMapping("/items/{id}")
    fun updateItem(
        @PathVariable id: Long,
        @Valid @RequestBody request: ItemRequest,
    ): ItemResponse {
        val item = inventoryService.update(id, request.toItem())
        return ItemResponse.from(item, categoryNamesById()[item.categoryId])
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteItem(@PathVariable id: Long) {
        inventoryService.delete(id)
    }

    private fun categoryNamesById(): Map<Long?, String> =
        categoryService.findAll().associate { it.id to it.name }
}

data class ItemRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:Min(value = 0, message = "Quantity must be zero or positive")
    val quantity: Int,

    val description: String? = null,
    val manufacturer: String? = null,
    val reference: String? = null,
    val categoryId: Long? = null,
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val productUrl: String? = null,
    val datasheetUrl: String? = null,

    @field:Min(value = 0, message = "Minimum quantity must be zero or positive")
    val minimumQuantity: Int = 0,
) {
    fun toItem(): Item = Item(
        name = name,
        quantity = quantity,
        description = description,
        manufacturer = manufacturer,
        reference = reference,
        categoryId = categoryId,
        tags = tags,
        notes = notes,
        productUrl = productUrl,
        datasheetUrl = datasheetUrl,
        minimumQuantity = minimumQuantity,
    )
}

data class ItemResponse(
    val id: Long?,
    val name: String,
    val quantity: Int,
    val description: String?,
    val manufacturer: String?,
    val reference: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val tags: List<String>,
    val notes: String?,
    val productUrl: String?,
    val datasheetUrl: String?,
    val minimumQuantity: Int,
) {
    companion object {
        fun from(item: Item, categoryName: String? = null): ItemResponse = ItemResponse(
            id = item.id,
            name = item.name,
            quantity = item.quantity,
            description = item.description,
            manufacturer = item.manufacturer,
            reference = item.reference,
            categoryId = item.categoryId,
            categoryName = categoryName,
            tags = item.tags,
            notes = item.notes,
            productUrl = item.productUrl,
            datasheetUrl = item.datasheetUrl,
            minimumQuantity = item.minimumQuantity,
        )
    }
}
