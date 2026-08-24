import { useEffect, useRef, useState } from 'react'
import { Alert, Box, Chip, Stack, Typography } from '@mui/material'
import * as adminApi from '../api/adminApi'

interface LocateMessage {
  id: number
  receivedAt: Date
  locations: { id: number; name: string; color: string }[]
}

const MAX_ENTRIES = 30

function parseLocations(raw: string): { id: number; name: string; color: string }[] {
  try {
    const parsed = JSON.parse(raw) as { locations?: { id: number; name: string; color: string }[] }
    return parsed.locations ?? []
  } catch {
    return []
  }
}

export function MqttLiveFeed() {
  const [connectionState, setConnectionState] = useState<'connecting' | 'open' | 'error'>('connecting')
  const [messages, setMessages] = useState<LocateMessage[]>([])
  const nextId = useRef(0)

  useEffect(() => {
    const source = new EventSource(adminApi.locateStreamUrl())

    source.onopen = () => setConnectionState('open')
    source.onerror = () => setConnectionState('error')
    source.addEventListener('locate', (event) => {
      const messageEvent = event as MessageEvent<string>
      setMessages((current) => {
        const entry: LocateMessage = {
          id: nextId.current++,
          receivedAt: new Date(),
          locations: parseLocations(messageEvent.data),
        }
        return [entry, ...current].slice(0, MAX_ENTRIES)
      })
    })

    return () => source.close()
  }, [])

  return (
    <Stack spacing={2.5}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        <Chip
          size="small"
          label={
            connectionState === 'open'
              ? 'Connecté'
              : connectionState === 'connecting'
                ? 'Connexion…'
                : 'Déconnecté'
          }
          color={connectionState === 'open' ? 'success' : connectionState === 'error' ? 'error' : 'default'}
        />
        <Typography variant="body2" color="textSecondary">
          Tapez dans la barre de recherche de l'inventaire pour voir des messages apparaître ici.
        </Typography>
      </Stack>

      {connectionState === 'error' && (
        <Alert severity="warning">
          Connexion au flux perdue — le serveur GridMind ou le broker MQTT est peut-être injoignable.
          Nouvelle tentative automatique.
        </Alert>
      )}

      {messages.length === 0 && connectionState === 'open' && (
        <Box sx={{ py: 4, textAlign: 'center' }}>
          <Typography color="textSecondary">En attente du premier message…</Typography>
        </Box>
      )}

      <Stack spacing={1}>
        {messages.map((message) => (
          <Stack
            key={message.id}
            direction="row"
            spacing={1.5}
            sx={{ alignItems: 'center', py: 1, px: 1.5, borderRadius: 1.5, bgcolor: 'background.default' }}
          >
            <Typography variant="caption" color="textSecondary" sx={{ minWidth: 72, flexShrink: 0 }}>
              {message.receivedAt.toLocaleTimeString()}
            </Typography>
            {message.locations.length === 0 ? (
              <Typography variant="body2" color="textSecondary">
                (aucun emplacement en surbrillance)
              </Typography>
            ) : (
              <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                {message.locations.map((location) => (
                  <Chip
                    key={location.id}
                    size="small"
                    label={location.name}
                    sx={{
                      bgcolor: location.color,
                      color: '#000',
                      fontWeight: 600,
                    }}
                  />
                ))}
              </Stack>
            )}
          </Stack>
        ))}
      </Stack>
    </Stack>
  )
}
