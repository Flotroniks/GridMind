import { fetchJson } from '@/lib/apiClient'

/** Publishes a locate highlight for whatever currently matches `query` — an empty/blank
 * query clears every highlight. Fire-and-forget from the caller's point of view: this is
 * a side effect of searching, not something search itself should ever fail over, so
 * callers are expected to swallow rejections rather than surface them as UI errors. */
export function locate(query: string | null | undefined): Promise<void> {
  const params = new URLSearchParams()
  if (query) params.set('query', query)
  return fetchJson<void>(`/api/locate?${params.toString()}`, { method: 'POST' })
}
