import { fetchJson } from '@/lib/apiClient'
import type { Category } from '../types/Category'

const BASE_PATH = '/api/categories'

export function listCategories(): Promise<Category[]> {
  return fetchJson<Category[]>(BASE_PATH)
}

export function createCategory(name: string): Promise<Category> {
  return fetchJson<Category>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify({ name }),
  })
}

export function renameCategory(id: number, name: string): Promise<Category> {
  return fetchJson<Category>(`${BASE_PATH}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ name }),
  })
}

export function deleteCategory(id: number): Promise<void> {
  return fetchJson<void>(`${BASE_PATH}/${id}`, { method: 'DELETE' })
}
