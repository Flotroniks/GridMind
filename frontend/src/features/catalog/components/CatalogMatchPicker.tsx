import { useEffect, useState } from 'react'
import { Alert, Box, Button, CircularProgress, Stack, Typography } from '@mui/material'
import { ApiError } from '@/lib/apiClient'
import * as catalogApi from '../api/catalogApi'
import type { CatalogResult } from '../types/CatalogResult'
import { CatalogResultGrid } from './CatalogResultGrid'

interface CatalogMatchPickerProps {
  /** Search terms to look up automatically, in parallel — typically an AI image
   * analysis's suggested queries (see `suggestSearchQueries`). */
  queries: string[]
  onSelect: (result: CatalogResult) => void
  /** Called when the user dismisses the picker without choosing anything — either there
   * was no good match, or they'd rather continue with what the AI analysis filled in. */
  onSkip: () => void
}

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function CatalogMatchPicker({ queries, onSelect, onSkip }: CatalogMatchPickerProps) {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [results, setResults] = useState<CatalogResult[]>([])

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    catalogApi
      .searchCatalogMany(queries)
      .then((data) => {
        if (!cancelled) setResults(data)
      })
      .catch((searchError: unknown) => {
        if (!cancelled) setError(messageOf(searchError, 'Recherche impossible.'))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [queries])

  return (
    <Stack spacing={2.5}>
      <Typography variant="body2" color="textSecondary">
        Recherche automatique dans les catalogues, à partir de l'analyse IA : « {queries.join(' » · « ')} »
      </Typography>

      {loading && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: 'text.secondary' }}>
          <CircularProgress size={16} />
          <Typography color="textSecondary">Recherche en cours…</Typography>
        </Stack>
      )}

      {!loading && error && <Alert severity="error">{error}</Alert>}

      {!loading && !error && results.length === 0 && (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 1.5,
            py: 5,
            px: 2,
            textAlign: 'center',
            border: '1px dashed',
            borderColor: 'divider',
            borderRadius: 2,
          }}
        >
          <Typography color="textSecondary">
            Aucune correspondance trouvée dans les catalogues. Vous pouvez continuer avec les
            informations de l'analyse IA, ou rechercher manuellement.
          </Typography>
        </Box>
      )}

      {!loading && results.length > 0 && <CatalogResultGrid results={results} onSelect={onSelect} />}

      {!loading && (
        <Stack direction="row" sx={{ justifyContent: results.length > 0 ? 'flex-start' : 'flex-end' }}>
          <Button onClick={onSkip}>
            {results.length > 0 ? 'Aucun de ceux-ci — continuer sans correspondance' : 'Continuer'}
          </Button>
        </Stack>
      )}
    </Stack>
  )
}
