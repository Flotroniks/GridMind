import { fetchJson } from '@/lib/apiClient'
import type { ItemStock } from '../types/ItemStock'
import type { StorageLocation } from '../types/StorageLocation'

const LOCATIONS_PATH = '/api/storage/locations'

export function listLocations(parentId?: number | null): Promise<StorageLocation[]> {
  const query = parentId != null ? `?parentId=${parentId}` : ''
  return fetchJson<StorageLocation[]>(`${LOCATIONS_PATH}${query}`)
}

export function getLocation(id: number): Promise<StorageLocation> {
  return fetchJson<StorageLocation>(`${LOCATIONS_PATH}/${id}`)
}

export function getLocationContents(id: number): Promise<ItemStock[]> {
  return fetchJson<ItemStock[]>(`${LOCATIONS_PATH}/${id}/contents`)
}

export function createLocation(name: string, parentId?: number | null): Promise<StorageLocation> {
  return fetchJson<StorageLocation>(LOCATIONS_PATH, {
    method: 'POST',
    body: JSON.stringify({ name, parentId }),
  })
}

export function renameLocation(id: number, name: string): Promise<StorageLocation> {
  return fetchJson<StorageLocation>(`${LOCATIONS_PATH}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ name }),
  })
}

export function configureLocationLed(
  id: number,
  controllerId: string | null,
  index: number | null,
): Promise<StorageLocation> {
  return fetchJson<StorageLocation>(`${LOCATIONS_PATH}/${id}/led`, {
    method: 'PATCH',
    body: JSON.stringify({ controllerId, index }),
  })
}

export function deleteLocation(id: number): Promise<void> {
  return fetchJson<void>(`${LOCATIONS_PATH}/${id}`, { method: 'DELETE' })
}

export interface FlatLocation {
  id: number
  path: string
}

/**
 * Walks the location tree from the root, one level of children at a time, building a
 * "Workshop / Drawer B" style path for each node. There's no backend endpoint for "all
 * locations" — the API only exposes children-of-a-parent — so this recurses through it.
 * Fine for a workshop-sized tree; the move-stock destination picker is the only
 * consumer, and it just needs a flat list to put in a `<select>`.
 */
async function collectLocations(parentId: number | null, prefix: string): Promise<FlatLocation[]> {
  const children = await listLocations(parentId)
  const results: FlatLocation[] = []
  for (const child of children) {
    const path = prefix ? `${prefix} / ${child.name}` : child.name
    results.push({ id: child.id, path })
    if (child.hasChildren) {
      results.push(...(await collectLocations(child.id, path)))
    }
  }
  return results
}

export function listAllLocationsFlat(): Promise<FlatLocation[]> {
  return collectLocations(null, '')
}

export function getItemStock(itemId: number): Promise<ItemStock[]> {
  return fetchJson<ItemStock[]>(`/api/storage/items/${itemId}/stock`)
}

export function allocateStock(
  itemId: number,
  storageLocationId: number,
  quantity: number,
): Promise<ItemStock> {
  return fetchJson<ItemStock>('/api/storage/stock/allocate', {
    method: 'POST',
    body: JSON.stringify({ itemId, storageLocationId, quantity }),
  })
}

export function moveStock(
  itemId: number,
  fromLocationId: number,
  toLocationId: number,
  quantity: number,
): Promise<void> {
  return fetchJson<void>('/api/storage/stock/move', {
    method: 'POST',
    body: JSON.stringify({ itemId, fromLocationId, toLocationId, quantity }),
  })
}
