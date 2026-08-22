import { fetchJson } from '@/lib/apiClient'
import type { Item, ItemFilters, ItemInput } from '../types/Item'

const BASE_PATH = '/api/inventory/items'

function toQueryString(filters?: ItemFilters): string {
  if (!filters) return ''
  const params = new URLSearchParams()
  if (filters.search) params.set('search', filters.search)
  if (filters.categoryId != null) params.set('categoryId', String(filters.categoryId))
  if (filters.manufacturer) params.set('manufacturer', filters.manufacturer)
  if (filters.status) params.set('status', filters.status)
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function listItems(filters?: ItemFilters): Promise<Item[]> {
  return fetchJson<Item[]>(`${BASE_PATH}${toQueryString(filters)}`)
}

export function getItem(id: number): Promise<Item> {
  return fetchJson<Item>(`${BASE_PATH}/${id}`)
}

export function createItem(input: ItemInput): Promise<Item> {
  return fetchJson<Item>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify(input),
  })
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
