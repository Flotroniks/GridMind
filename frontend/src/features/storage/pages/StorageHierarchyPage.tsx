import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import LightbulbOutlinedIcon from '@mui/icons-material/LightbulbOutlined'
import SwapHorizIcon from '@mui/icons-material/SwapHoriz'
import {
  Box,
  Button,
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useToast } from '@/components/common/useToast'
import { ApiError } from '@/lib/apiClient'
import * as storageApi from '../api/storageApi'
import { CreateLocationForm } from '../components/CreateLocationForm'
import { LocationContents } from '../components/LocationContents'
import { LocationTree } from '../components/LocationTree'
import { MoveStockDialog } from '../components/MoveStockDialog'

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function StorageHierarchyPage() {
  const { showToast } = useToast()
  const queryClient = useQueryClient()
  const [breadcrumb, setBreadcrumb] = useState<{ id: number; name: string }[]>([])
  const [movingStock, setMovingStock] = useState(false)
  const [renamingCurrent, setRenamingCurrent] = useState(false)
  const [currentNameDraft, setCurrentNameDraft] = useState('')
  const [configuringLed, setConfiguringLed] = useState(false)
  const [ledControllerDraft, setLedControllerDraft] = useState('')
  const [ledIndexDraft, setLedIndexDraft] = useState('')

  const currentId = breadcrumb.length > 0 ? breadcrumb[breadcrumb.length - 1].id : null
  const parentIdOfCurrent = breadcrumb.length > 1 ? breadcrumb[breadcrumb.length - 2].id : null

  // Fetched separately from the breadcrumb (which only carries id/name) — this is the
  // only place the current location's LED mapping is needed, to prefill and reflect the
  // dialog below.
  const { data: currentLocation } = useQuery({
    queryKey: ['storage', 'location', currentId],
    queryFn: () => storageApi.getLocation(currentId!),
    enabled: currentId != null,
  })

  const handleSelect = (id: number, name: string) => {
    setBreadcrumb((current) => [...current, { id, name }])
  }

  const handleNavigateBreadcrumb = (index: number) => {
    setBreadcrumb((current) => (index < 0 ? [] : current.slice(0, index + 1)))
  }

  const renameMutation = useMutation({
    mutationFn: () => storageApi.renameLocation(currentId!, currentNameDraft.trim()),
    onSuccess: (updated) => {
      showToast('Emplacement renommé.', 'success')
      setBreadcrumb((current) => current.map((crumb, index) => (index === current.length - 1 ? { ...crumb, name: updated.name } : crumb)))
      setRenamingCurrent(false)
      // The renamed location shows up as a child in its parent's list too (whichever
      // level that's cached under — null for a root-level location).
      void queryClient.invalidateQueries({ queryKey: ['storage', 'children', parentIdOfCurrent] })
      void queryClient.invalidateQueries({ queryKey: ['storage', 'allLocations'] })
      void queryClient.invalidateQueries({ queryKey: ['storage', 'location', currentId] })
    },
    onError: (error: unknown) => showToast(messageOf(error, 'Renommage impossible.'), 'error'),
  })

  const startRenamingCurrent = () => {
    if (currentId == null) return
    setCurrentNameDraft(breadcrumb[breadcrumb.length - 1].name)
    setRenamingCurrent(true)
  }

  const commitRenameCurrent = () => {
    if (!currentNameDraft.trim()) return
    renameMutation.mutate()
  }

  const configureLedMutation = useMutation({
    mutationFn: (params: { controllerId: string | null; index: number | null }) =>
      storageApi.configureLocationLed(currentId!, params.controllerId, params.index),
    onSuccess: () => {
      showToast('LED mise à jour.', 'success')
      setConfiguringLed(false)
      void queryClient.invalidateQueries({ queryKey: ['storage', 'location', currentId] })
    },
    onError: (error: unknown) => showToast(messageOf(error, 'Configuration LED impossible.'), 'error'),
  })

  const openLedConfig = () => {
    if (currentId == null) return
    setLedControllerDraft(currentLocation?.ledControllerId ?? '')
    setLedIndexDraft(currentLocation?.ledIndex != null ? String(currentLocation.ledIndex) : '')
    setConfiguringLed(true)
  }

  const ledMismatch = (ledControllerDraft.trim() !== '') !== (ledIndexDraft.trim() !== '')

  return (
    <Box sx={{ mx: 'auto', width: { xs: '100%', sm: '75vw' }, maxWidth: 1600 }}>
      <Card variant="outlined" sx={{ backdropFilter: 'blur(8px)', borderColor: 'divider' }}>
        <CardContent sx={{ py: 3, px: { xs: 3, sm: 5, md: 8 } }}>
          <Box sx={{ mb: 3 }}>
            <Typography variant="overline" color="primary" sx={{ fontWeight: 600, letterSpacing: 2 }}>
              GridMind
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>
              Stockage
            </Typography>
          </Box>

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: { xs: 4, md: 5 } }}>
            <Box>
              <Stack direction="row" spacing={1} sx={{ mb: 1.5, alignItems: 'center' }}>
                <AccountTreeOutlinedIcon fontSize="small" color="action" />
                <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                  Emplacements
                </Typography>
              </Stack>

              <LocationTree
                parentId={currentId}
                breadcrumb={breadcrumb}
                onSelect={handleSelect}
                onNavigateBreadcrumb={handleNavigateBreadcrumb}
              />
              <Box sx={{ mt: 2 }}>
                <CreateLocationForm parentId={currentId} />
              </Box>
            </Box>

            <Box sx={{ borderLeft: { md: '1px solid' }, borderColor: 'divider', pl: { md: 5 } }}>
              <Stack
                direction="row"
                sx={{ mb: 1.5, alignItems: 'center', justifyContent: 'space-between', minHeight: 32 }}
              >
                {currentId != null && renamingCurrent ? (
                  <TextField
                    size="small"
                    value={currentNameDraft}
                    onChange={(event) => setCurrentNameDraft(event.target.value)}
                    autoFocus
                    fullWidth
                    onKeyDown={(event) => event.key === 'Enter' && commitRenameCurrent()}
                    onBlur={commitRenameCurrent}
                  />
                ) : (
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                    <Inventory2OutlinedIcon fontSize="small" color="action" />
                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                      {currentId != null ? breadcrumb[breadcrumb.length - 1].name : 'Contenu'}
                    </Typography>
                    {currentId != null && (
                      <IconButton size="small" aria-label="Renommer" sx={{ color: 'text.disabled' }} onClick={startRenamingCurrent}>
                        <EditOutlinedIcon fontSize="small" />
                      </IconButton>
                    )}
                    {currentId != null && (
                      <IconButton
                        size="small"
                        aria-label="Configurer la LED"
                        sx={{ color: currentLocation?.ledControllerId ? 'warning.main' : 'text.disabled' }}
                        onClick={openLedConfig}
                      >
                        <LightbulbOutlinedIcon fontSize="small" />
                      </IconButton>
                    )}
                  </Stack>
                )}

                {currentId != null && !renamingCurrent && (
                  <Button size="small" startIcon={<SwapHorizIcon fontSize="small" />} onClick={() => setMovingStock(true)}>
                    Déplacer
                  </Button>
                )}
              </Stack>
              <LocationContents locationId={currentId} />
            </Box>
          </Box>
        </CardContent>
      </Card>

      <MoveStockDialog
        open={movingStock}
        fromLocationId={currentId}
        onClose={() => setMovingStock(false)}
      />

      <Dialog open={configuringLed} onClose={() => setConfiguringLed(false)}>
        <DialogTitle>LED de l'emplacement</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1, minWidth: 320 }}>
            <Typography variant="body2" color="textSecondary">
              Associe cet emplacement à une LED physique pour la fonction « localiser ».
              Laisser les deux champs vides pour ne rien allumer ici.
            </Typography>
            <TextField
              label="Contrôleur"
              size="small"
              value={ledControllerDraft}
              onChange={(event) => setLedControllerDraft(event.target.value)}
              placeholder="ex. strip-a"
              fullWidth
              autoComplete="off"
            />
            <TextField
              label="Index LED"
              size="small"
              type="number"
              value={ledIndexDraft}
              onChange={(event) => setLedIndexDraft(event.target.value)}
              fullWidth
              autoComplete="off"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button
            color="inherit"
            onClick={() => configureLedMutation.mutate({ controllerId: null, index: null })}
          >
            Effacer
          </Button>
          <Button onClick={() => setConfiguringLed(false)}>Annuler</Button>
          <Button
            variant="contained"
            disabled={ledMismatch}
            onClick={() =>
              configureLedMutation.mutate({
                controllerId: ledControllerDraft.trim() || null,
                index: ledIndexDraft.trim() === '' ? null : Number(ledIndexDraft),
              })
            }
          >
            Enregistrer
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
