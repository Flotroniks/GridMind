import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome'
import SearchIcon from '@mui/icons-material/Search'
import { Alert, Box, Button, CircularProgress, Stack, TextField, Typography } from '@mui/material'
import { ApiError } from '@/lib/apiClient'
import * as catalogApi from '../api/catalogApi'
import type { CatalogResult } from '../types/CatalogResult'
import { CatalogResultGrid } from './CatalogResultGrid'

interface CatalogSearchStepProps {
  onSelect: (result: CatalogResult) => void
  onManualCreateRequest: (query: string) => void
  onAnalyzePhotoRequest: () => void
  /** Pre-fills and immediately runs a search — used when arriving here with a keyword
   * suggested by the image-analysis feature, instead of starting from an empty search. */
  initialQuery?: string
}

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function CatalogSearchStep({
  onSelect,
  onManualCreateRequest,
  onAnalyzePhotoRequest,
  initialQuery,
}: CatalogSearchStepProps) {
  const [query, setQuery] = useState(initialQuery ?? '')
  const [results, setResults] = useState<CatalogResult[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [searched, setSearched] = useState(false)

  useEffect(() => {
    const term = query.trim()
    if (!term) {
      setResults([])
      setError(null)
      setSearched(false)
      return
    }

    const timeout = setTimeout(() => {
      setLoading(true)
      catalogApi
        .searchCatalog(term)
        .then((data) => {
          setResults(data)
          setError(null)
        })
        .catch((searchError: unknown) => setError(messageOf(searchError, 'Recherche impossible.')))
        .finally(() => {
          setLoading(false)
          setSearched(true)
        })
    }, 250)

    return () => clearTimeout(timeout)
  }, [query])

  return (
    <Stack spacing={2.5}>
      <TextField
        autoFocus
        placeholder="Ex. ESP32-S3-DEVKITC-1-N16R8, STM32F103C8T6, BME280…"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        autoComplete="off"
        fullWidth
        slotProps={{
          input: {
            startAdornment: <SearchIcon color="action" sx={{ fontSize: 22, mr: 1 }} />,
          },
        }}
        sx={{
          '& .MuiOutlinedInput-root': {
            borderRadius: 999,
            bgcolor: 'background.default',
            '& fieldset': { borderColor: 'divider' },
            '&:hover fieldset': { borderColor: 'text.secondary' },
            '&.Mui-focused fieldset': { borderColor: 'primary.main' },
          },
        }}
      />

      <Stack direction="row" spacing={1.5} sx={{ flexWrap: 'wrap' }}>
        <Button variant="outlined" startIcon={<AddIcon />} onClick={() => onManualCreateRequest(query.trim())}>
          Tout faire manuellement
        </Button>
        <Button variant="outlined" startIcon={<AutoAwesomeIcon />} onClick={onAnalyzePhotoRequest}>
          Analyser une photo (IA)
        </Button>
      </Stack>

      {error && <Alert severity="error">{error}</Alert>}

      {loading && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: 'text.secondary' }}>
          <CircularProgress size={16} />
          <Typography color="textSecondary">Recherche en cours…</Typography>
        </Stack>
      )}

      {!loading && searched && results.length === 0 && !error && (
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
            Aucun résultat pour « {query.trim()} ». Vous pouvez continuer manuellement ou via une photo
            ci-dessus.
          </Typography>
        </Box>
      )}

      {!loading && results.length > 0 && <CatalogResultGrid results={results} onSelect={onSelect} />}

      {!loading && !searched && (
        <Typography color="textSecondary" sx={{ textAlign: 'center', py: 4 }}>
          Tapez un nom, une référence ou un MPN pour rechercher dans les catalogues.
        </Typography>
      )}
    </Stack>
  )
}
