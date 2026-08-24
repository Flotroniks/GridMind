import { useEffect, useRef, useState } from 'react'
import SensorsIcon from '@mui/icons-material/Sensors'
import { Alert, Chip, List, ListItem, Stack, Typography } from '@mui/material'
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

      {messages.length === 0 ? (
        <Stack
          spacing={1}
          sx={{
            alignItems: 'center',
            py: 4,
            px: 2,
            textAlign: 'center',
            border: '1px dashed',
            borderColor: 'divider',
            borderRadius: 2,
          }}
        >
          <SensorsIcon sx={{ fontSize: 32, color: 'text.disabled' }} />
          <Typography variant="body2" color="textSecondary">
            En attente du premier message…
          </Typography>
        </Stack>
      ) : (
        <List
          disablePadding
          sx={{ bgcolor: 'background.default', borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}
        >
          {messages.map((message, index) => (
            <ListItem
              key={message.id}
              sx={{ gap: 1.5, py: 1, borderTop: index > 0 ? '1px solid' : 'none', borderColor: 'divider' }}
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
            </ListItem>
          ))}
        </List>
      )}
    </Stack>
  )
}
