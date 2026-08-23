package org.gridmind.backend.inventory.api

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.gridmind.backend.category.application.CategoryService
import org.gridmind.backend.inventory.application.ImageStorageService
import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.inventory.domain.Item
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/inventory")
class ItemController(
    private val inventoryService: InventoryService,
    private val categoryService: CategoryService,
    private val imageStorageService: ImageStorageService,
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

    // Resolving the image here, before InventoryService ever sees the item, keeps
    // InventoryService free of any notion of "external source" — it only ever persists
    // an Item, which already carries a plain imageId like any other field.
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun createItem(@Valid @RequestBody request: ItemRequest): ItemResponse {
        val imageId = request.sourceImageUrl
            ?.let { imageStorageService.downloadAndStore(it, request.sourceImageProvider) }
            ?.id
        val item = inventoryService.create(request.toItem(imageId = imageId))
        return ItemResponse.from(item, categoryNamesById()[item.categoryId])
    }

    // Separate from createItem: only the flow that has a real local file to hand over
    // (the image-analysis prototype) uses this one. Everything else — manual entry,
    // catalog search with a provider image URL — keeps using the plain JSON endpoint.
    @PostMapping("/items/with-photo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createItemWithPhoto(
        @Valid @RequestPart("item") request: ItemRequest,
        @RequestPart(value = "image", required = false) image: MultipartFile?,
    ): ItemResponse {
        val imageId = image?.takeIf { !it.isEmpty }
            ?.let { imageStorageService.storeUploaded(it.bytes, it.contentType ?: "application/octet-stream", "Analyse IA locale") }
            ?.id
        val item = inventoryService.create(request.toItem(imageId = imageId))
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

    @field:Min(value = 0, message = "HS quantity must be zero or positive")
    val quantityHs: Int = 0,

    @field:Min(value = 0, message = "In-use quantity must be zero or positive")
    val quantityInUse: Int = 0,

    // Not part of the Item domain — consumed once by ItemController when creating an
    // item, to resolve a locally stored imageId before InventoryService ever runs.
    val sourceImageUrl: String? = null,
    val sourceImageProvider: String? = null,
) {
    fun toItem(imageId: Long? = null): Item = Item(
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
        quantityHs = quantityHs,
        quantityInUse = quantityInUse,
        imageId = imageId,
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
    val quantityHs: Int,
    val quantityInUse: Int,
    val quantityAvailable: Int,
    val imageUrl: String?,
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
            quantityHs = item.quantityHs,
            quantityInUse = item.quantityInUse,
            quantityAvailable = item.quantityAvailable,
            imageUrl = item.imageId?.let { "/api/media/$it" },
        )
    }
}
