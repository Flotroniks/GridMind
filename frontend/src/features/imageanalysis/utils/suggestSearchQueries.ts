import type { ImageAnalysisResult } from '../types/ImageAnalysis'

/** Candidate search terms to automatically look up in the catalog providers right after
 * an AI analysis — the AI's own `searchQueries` when it provided any, otherwise a
 * best-effort fallback built from whatever identifying fields it did fill in. Returns an
 * empty array when there's nothing at all worth searching for. */
export function suggestSearchQueries(result: ImageAnalysisResult): string[] {
  if (result.searchQueries.length > 0) return result.searchQueries

  const fallback = [result.manufacturer, result.model].filter(Boolean).join(' ').trim() || result.name?.trim()

  return fallback ? [fallback] : []
}
