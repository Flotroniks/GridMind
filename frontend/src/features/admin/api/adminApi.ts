import { BASE_URL, fetchJson } from '@/lib/apiClient'
import type { SystemStatus } from '../types/SystemStatus'

export function fetchStatus(): Promise<SystemStatus> {
  return fetchJson<SystemStatus>('/api/admin/status')
}

/** Absolute URL for the MQTT locate live-view SSE stream — `EventSource` needs a real
 * URL, not a fetch-wrapped call. */
export function locateStreamUrl(): string {
  return `${BASE_URL}/api/admin/locate/stream`
}
