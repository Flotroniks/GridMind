import { fetchJson, postMultipart } from '@/lib/apiClient'
import type { Item, ItemFilters, ItemInput } from '../types/Item'

const BASE_PATH = '/api/inventory/items'

function toQueryString(filters?: ItemFilters): string {
  if (!filters) return ''
  const params = new URLSearchParams()
  if (filters.search) params.set('search', filters.search)
  if (filters.categoryId != null) params.set('categoryId', String(filters.categoryId))
  if (filters.manufacturer) params.set('manufacturer', filters.manufacturer)
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function listItems(filters?: ItemFilters): Promise<Item[]> {
  return fetchJson<Item[]>(`${BASE_PATH}${toQueryString(filters)}`)
}

export function getItem(id: number): Promise<Item> {
  return fetchJson<Item>(`${BASE_PATH}/${id}`)
}

export interface CreateItemOptions {
  sourceImageUrl?: string
  sourceImageProvider?: string
}

export function createItem(input: ItemInput, options?: CreateItemOptions): Promise<Item> {
  return fetchJson<Item>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify({ ...input, ...options }),
  })
}

/** Creates an item with a locally uploaded photo (e.g. from the image-analysis flow) as
 * its image, instead of one downloaded from a catalog provider's URL. */
export function createItemWithPhoto(input: ItemInput, photo: File): Promise<Item> {
  const formData = new FormData()
  formData.append('item', new Blob([JSON.stringify(input)], { type: 'application/json' }))
  formData.append('image', photo)
  return postMultipart<Item>(`${BASE_PATH}/with-photo`, formData)
}

export function updateItem(id: number, input: ItemInput): Promise<Item> {
  return fetchJson<Item>(`${BASE_PATH}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(input),
  })
}

export function deleteItem(id: number): Promise<void> {
  return fetchJson<void>(`${BASE_PATH}/${id}`, {
    method: 'DELETE',
  })
}
