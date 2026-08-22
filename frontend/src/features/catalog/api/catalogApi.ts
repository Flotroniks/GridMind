import { fetchJson } from '@/lib/apiClient'
import type { CatalogResult } from '../types/CatalogResult'

const BASE_PATH = '/api/catalog/search'

export function searchCatalog(query: string): Promise<CatalogResult[]> {
  const params = new URLSearchParams({ query })
  return fetchJson<CatalogResult[]>(`${BASE_PATH}?${params.toString()}`)
}
