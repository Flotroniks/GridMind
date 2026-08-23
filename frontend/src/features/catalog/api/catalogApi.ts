import { fetchJson } from '@/lib/apiClient'
import type { CatalogResult } from '../types/CatalogResult'

const BASE_PATH = '/api/catalog/search'

export function searchCatalog(query: string): Promise<CatalogResult[]> {
  const params = new URLSearchParams({ query })
  return fetchJson<CatalogResult[]>(`${BASE_PATH}?${params.toString()}`)
}

/** Runs several queries in parallel server-side and returns one deduplicated list —
 * used to automatically search for candidate matches to an AI image analysis, which can
 * suggest more than one plausible search term. */
export function searchCatalogMany(queries: string[]): Promise<CatalogResult[]> {
  const params = new URLSearchParams()
  for (const query of queries) params.append('queries', query)
  return fetchJson<CatalogResult[]>(`${BASE_PATH}-many?${params.toString()}`)
}
