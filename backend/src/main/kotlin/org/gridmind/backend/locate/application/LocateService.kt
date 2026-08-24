package org.gridmind.backend.locate.application

import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.locate.domain.LocateHighlight
import org.gridmind.backend.storage.application.StockAllocationService
import org.gridmind.backend.storage.application.StorageLocationService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Turns a search term into "which storage locations currently match, highlighted in one
 * fixed color" and hands that off to [LocatePublisherPort] — the same search the
 * inventory search bar already runs, reused here as the trigger for a physical
 * pick-to-light highlight instead of introducing a second, separate search concept.
 */
@Service
class LocateService(
    private val inventoryService: InventoryService,
    private val stockAllocationService: StockAllocationService,
    private val storageLocationService: StorageLocationService,
    private val locatePublisherPort: LocatePublisherPort,
    @Value("\${gridmind.locate.highlight-color}") private val highlightColor: String,
) {
    @Transactional(readOnly = true)
    fun locate(query: String?) {
        // A blank query matches every item in InventoryService.search — which would
        // highlight every location instead of clearing them, the opposite of what an
        // emptied search box should do.
        val matchingItems = if (query.isNullOrBlank()) emptyList() else inventoryService.search(query, null, null)

        val locationIds = matchingItems
            .mapNotNull { it.id }
            .flatMap(stockAllocationService::stockByItem)
            // A zero-quantity row is a location that used to hold this item but doesn't
            // anymore — StockAllocationService.allocate overwrites in place rather than
            // deleting, so this filter is what actually keeps an emptied-out location dark.
            .filter { it.quantity > 0 }
            .map { it.storageLocationId }
            .distinct()

        val highlights = locationIds.map { id ->
            val location = storageLocationService.findById(id)
            LocateHighlight(storageLocationId = id, storageLocationName = location.name, color = highlightColor)
        }

        locatePublisherPort.publish(highlights)
    }
}
