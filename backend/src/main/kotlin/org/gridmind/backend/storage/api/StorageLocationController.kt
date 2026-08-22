package org.gridmind.backend.storage.api

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.storage.application.StockAllocationService
import org.gridmind.backend.storage.application.StorageLocationService
import org.gridmind.backend.storage.domain.ItemStock
import org.gridmind.backend.storage.domain.StorageLocation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/storage/locations")
class StorageLocationController(
    private val storageLocationService: StorageLocationService,
    private val stockAllocationService: StockAllocationService,
    private val inventoryService: InventoryService,
) {
    @GetMapping
    fun listLocations(@RequestParam(required = false) parentId: Long?): List<StorageLocationResponse> =
        storageLocationService.findChildren(parentId).map(::toResponse)

    @GetMapping("/{id}")
    fun getLocation(@PathVariable id: Long): StorageLocationResponse =
        toResponse(storageLocationService.findById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createLocation(@Valid @RequestBody request: CreateStorageLocationRequest): StorageLocationResponse =
        toResponse(storageLocationService.create(request.name, request.parentId))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLocation(@PathVariable id: Long) {
        storageLocationService.delete(id)
    }

    @GetMapping("/{id}/contents")
    fun getContents(@PathVariable id: Long): List<ItemStockResponse> {
        val locationName = storageLocationService.findById(id).name
        return stockAllocationService.stockByLocation(id).map { stock -> toStockResponse(stock, locationName) }
    }

    private fun toResponse(location: StorageLocation): StorageLocationResponse =
        StorageLocationResponse(
            id = location.id,
            name = location.name,
            parentId = location.parentId,
            hasChildren = location.id?.let { storageLocationService.findChildren(it).isNotEmpty() } ?: false,
        )

    private fun toStockResponse(stock: ItemStock, locationName: String?): ItemStockResponse {
        val item = inventoryService.findById(stock.itemId)
        return ItemStockResponse(
            itemId = stock.itemId,
            itemName = item.name,
            storageLocationId = stock.storageLocationId,
            storageLocationName = locationName,
            quantity = stock.quantity,
        )
    }
}

data class CreateStorageLocationRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    val parentId: Long? = null,
)

data class StorageLocationResponse(
    val id: Long?,
    val name: String,
    val parentId: Long?,
    val hasChildren: Boolean,
)
