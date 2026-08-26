import { BASE_URL, fetchJson } from '@/lib/apiClient'
import type { BackupSummary } from '../types/Backup'
import type { SystemStatus } from '../types/SystemStatus'

export function fetchStatus(): Promise<SystemStatus> {
  return fetchJson<SystemStatus>('/api/admin/status')
}

/** Absolute URL for the MQTT locate live-view SSE stream — `EventSource` needs a real
 * URL, not a fetch-wrapped call. */
export function locateStreamUrl(): string {
  return `${BASE_URL}/api/admin/locate/stream`
}

/** Absolute URL for the backup download — the response carries its own
 * `Content-Disposition: attachment` header, so a plain navigation/anchor click is
 * enough to save it; no need to fetch it as a blob first. */
export function exportBackupUrl(): string {
  return `${BASE_URL}/api/backup/export`
}

/** [snapshotJson] is sent as-is: it's already a JSON document read straight from the
 * user's chosen file, not a JS object that needs stringifying. */
export function importBackup(snapshotJson: string): Promise<BackupSummary> {
  return fetchJson<BackupSummary>('/api/backup/import', { method: 'POST', body: snapshotJson })
}

export function clearBackup(): Promise<void> {
  return fetchJson<void>('/api/backup/clear', { method: 'POST' })
}
