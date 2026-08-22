import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import SearchIcon from '@mui/icons-material/Search'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { ApiError } from '@/lib/apiClient'
import * as catalogApi from '../api/catalogApi'
import type { CatalogResult } from '../types/CatalogResult'

interface CatalogSearchStepProps {
  onSelect: (result: CatalogResult) => void
  onManualCreateRequest: (query: string) => void
}

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function CatalogSearchStep({ onSelect, onManualCreateRequest }: CatalogSearchStepProps) {
  const [query, setQuery] = useState('')
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
          <Typography color="textSecondary">Aucun résultat pour « {query.trim()} ».</Typography>
          <Button variant="outlined" startIcon={<AddIcon />} onClick={() => onManualCreateRequest(query.trim())}>
            Créer manuellement
          </Button>
        </Box>
      )}

      {!loading && results.length > 0 && (
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
            gap: 2,
          }}
        >
          {results.map((result) => (
            <Card
              key={`${result.manufacturer ?? ''}-${result.mpn ?? result.name}`}
              variant="outlined"
              sx={{ overflow: 'hidden', borderColor: 'divider', display: 'flex', flexDirection: 'column' }}
            >
              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  aspectRatio: '4 / 3',
                  bgcolor: 'background.default',
                  color: 'text.disabled',
                }}
              >
                {result.images[0] ? (
                  <Box
                    component="img"
                    src={result.images[0].url}
                    alt={result.name}
                    sx={{ width: '100%', height: '100%', objectFit: 'contain' }}
                  />
                ) : (
                  <ImageOutlinedIcon sx={{ fontSize: 48 }} />
                )}
              </Box>

              <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, flexGrow: 1 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                  {result.name}
                </Typography>
                {result.manufacturer && (
                  <Typography variant="body2" color="textSecondary">
                    {result.manufacturer}
                  </Typography>
                )}
                {result.mpn && (
                  <Typography variant="body2" color="textSecondary">
                    {result.mpn}
                  </Typography>
                )}

                {result.sources.length > 0 && (
                  <Typography variant="caption" color="textSecondary" sx={{ mt: 0.5 }}>
                    Sources : {result.sources.join(' • ')}
                  </Typography>
                )}

                <Box sx={{ flexGrow: 1 }} />

                <Stack direction="row" sx={{ justifyContent: 'flex-end', mt: 1.5 }}>
                  <Button variant="contained" size="small" onClick={() => onSelect(result)}>
                    Sélectionner
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          ))}
        </Box>
      )}

      {!loading && !searched && (
        <Typography color="textSecondary" sx={{ textAlign: 'center', py: 4 }}>
          Tapez un nom, une référence ou un MPN pour rechercher dans les catalogues.
        </Typography>
      )}
    </Stack>
  )
}
