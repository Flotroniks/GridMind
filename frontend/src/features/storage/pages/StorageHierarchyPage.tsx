import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import EditIcon from '@mui/icons-material/Edit'
import { Box, Button, Card, CardContent, IconButton, Stack, TextField, Typography } from '@mui/material'
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

  const currentId = breadcrumb.length > 0 ? breadcrumb[breadcrumb.length - 1].id : null
  const parentIdOfCurrent = breadcrumb.length > 1 ? breadcrumb[breadcrumb.length - 2].id : null

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

  return (
    <Box sx={{ mx: 'auto', width: { xs: '100%', sm: '75vw' }, maxWidth: 1600 }}>
      <Card variant="outlined" sx={{ backdropFilter: 'blur(8px)', borderColor: 'divider' }}>
        <CardContent sx={{ py: 3, px: { xs: 3, sm: 5, md: 8 } }}>
          <Box sx={{ mb: 2 }}>
            <Typography variant="overline" color="primary" sx={{ fontWeight: 600, letterSpacing: 2 }}>
              GridMind
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>
              Stockage
            </Typography>
          </Box>

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 3 }}>
            <Box>
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

            <Box>
              {currentId != null &&
                (renamingCurrent ? (
                  <TextField
                    size="small"
                    value={currentNameDraft}
                    onChange={(event) => setCurrentNameDraft(event.target.value)}
                    autoFocus
                    fullWidth
                    sx={{ mb: 1.5 }}
                    onKeyDown={(event) => event.key === 'Enter' && commitRenameCurrent()}
                    onBlur={commitRenameCurrent}
                  />
                ) : (
                  <Stack direction="row" spacing={0.5} sx={{ mb: 1.5, alignItems: 'center' }}>
                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                      {breadcrumb[breadcrumb.length - 1].name}
                    </Typography>
                    <IconButton size="small" aria-label="Renommer" onClick={startRenamingCurrent}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Stack>
                ))}

              <Stack
                direction="row"
                sx={{ mb: 1, alignItems: 'center', justifyContent: 'space-between' }}
              >
                <Typography sx={{ fontWeight: 600 }}>Contenu</Typography>
                {currentId != null && (
                  <Button size="small" onClick={() => setMovingStock(true)}>
                    Déplacer du stock
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
    </Box>
  )
}
