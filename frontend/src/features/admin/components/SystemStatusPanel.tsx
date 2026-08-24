import { useCallback, useEffect, useState } from 'react'
import RefreshIcon from '@mui/icons-material/Refresh'
import { Alert, Button, CircularProgress, Divider, Stack, Typography } from '@mui/material'
import { ApiError } from '@/lib/apiClient'
import * as adminApi from '../api/adminApi'
import type { SystemStatus } from '../types/SystemStatus'
import { IntegrationStatusList } from './IntegrationStatusList'

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function SystemStatusPanel() {
  const [status, setStatus] = useState<SystemStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    setLoading(true)
    setError(null)
    adminApi
      .fetchStatus()
      .then(setStatus)
      .catch((loadError: unknown) => setError(messageOf(loadError, 'Impossible de charger le statut.')))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  return (
    <Stack spacing={2.5}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="body2" color="textSecondary">
          Point de vue en direct des intégrations externes — mots de passe/clés jamais affichés, juste
          leur présence et leur joignabilité.
        </Typography>
        <Button size="small" startIcon={<RefreshIcon />} onClick={load} disabled={loading}>
          Actualiser
        </Button>
      </Stack>

      {error && <Alert severity="error">{error}</Alert>}

      {loading && !status && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: 'text.secondary' }}>
          <CircularProgress size={16} />
          <Typography color="textSecondary">Chargement…</Typography>
        </Stack>
      )}

      {status && (
        <>
          <Typography variant="subtitle2" color="textSecondary">
            Catalogues produits
          </Typography>
          <IntegrationStatusList statuses={status.catalogProviders} />

          <Divider />

          <Typography variant="subtitle2" color="textSecondary">
            Analyse IA et localisation
          </Typography>
          <IntegrationStatusList statuses={[status.ollama, status.mqtt]} />
        </>
      )}
    </Stack>
  )
}
