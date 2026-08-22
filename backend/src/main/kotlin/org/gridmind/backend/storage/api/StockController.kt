package org.gridmind.backend.storage.api

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.storage.application.StockAllocationService
import org.gridmind.backend.storage.application.StorageLocationService
import org.gridmind.backend.storage.domain.ItemStock
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/storage")
class StockController(
    private val stockAllocationService: StockAllocationService,
    private val inventoryService: InventoryService,
    private val storageLocationService: StorageLocationService,
) {
    @GetMapping("/items/{itemId}/stock")
    fun getStockForItem(@PathVariable itemId: Long): List<ItemStockResponse> =
        stockAllocationService.stockByItem(itemId).map(::toResponse)

    @PostMapping("/stock/allocate")
    fun allocate(@Valid @RequestBody request: AllocateStockRequest): ItemStockResponse =
        toResponse(
            stockAllocationService.allocate(request.itemId, request.storageLocationId, request.quantity),
        )

    @PostMapping("/stock/move")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun move(@Valid @RequestBody request: MoveStockRequest) {
        stockAllocationService.moveStock(
            request.itemId,
            request.fromLocationId,
            request.toLocationId,
            request.quantity,
        )
    }

    private fun toResponse(stock: ItemStock): ItemStockResponse {
        val item = inventoryService.findById(stock.itemId)
        val location = storageLocationService.findById(stock.storageLocationId)
        return ItemStockResponse(
            itemId = stock.itemId,
            itemName = item.name,
            storageLocationId = stock.storageLocationId,
            storageLocationName = location.name,
            quantity = stock.quantity,
        )
    }
}

data class AllocateStockRequest(
    @field:NotNull val itemId: Long,
    @field:NotNull val storageLocationId: Long,
    @field:Min(0) val quantity: Int,
)

data class MoveStockRequest(
    @field:NotNull val itemId: Long,
    @field:NotNull val fromLocationId: Long,
    @field:NotNull val toLocationId: Long,
    @field:Min(1) val quantity: Int,
)

data class ItemStockResponse(
    val itemId: Long,
    val itemName: String,
    val storageLocationId: Long,
    val storageLocationName: String?,
    val quantity: Int,
)
