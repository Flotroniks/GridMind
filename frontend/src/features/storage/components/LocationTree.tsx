import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import EditIcon from '@mui/icons-material/Edit'
import {
  Box,
  Breadcrumbs,
  CircularProgress,
  IconButton,
  Link as MuiLink,
  List,
  ListItemButton,
  ListItemText,
  TextField,
  Typography,
} from '@mui/material'
import { useToast } from '@/components/common/useToast'
import { ApiError } from '@/lib/apiClient'
import * as storageApi from '../api/storageApi'

interface LocationTreeProps {
  parentId: number | null
  breadcrumb: { id: number; name: string }[]
  onSelect: (id: number, name: string) => void
  onNavigateBreadcrumb: (index: number) => void
}

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function LocationTree({
  parentId,
  breadcrumb,
  onSelect,
  onNavigateBreadcrumb,
}: LocationTreeProps) {
  const { showToast } = useToast()
  const queryClient = useQueryClient()
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')

  const { data: children = [], isLoading } = useQuery({
    queryKey: ['storage', 'children', parentId],
    queryFn: () => storageApi.listLocations(parentId),
  })

  const renameMutation = useMutation({
    mutationFn: ({ id, name }: { id: number; name: string }) => storageApi.renameLocation(id, name),
    onSuccess: () => {
      showToast('Emplacement renommé.', 'success')
      setEditingId(null)
      void queryClient.invalidateQueries({ queryKey: ['storage', 'children', parentId] })
      // Renaming also touches whatever's cached under this location's own id (its
      // contents view, and any item-stock list showing this location's name).
      void queryClient.invalidateQueries({ queryKey: ['storage', 'contents'] })
      void queryClient.invalidateQueries({ queryKey: ['storage', 'itemStock'] })
      void queryClient.invalidateQueries({ queryKey: ['storage', 'allLocations'] })
    },
    onError: (error: unknown) => showToast(messageOf(error, 'Renommage impossible.'), 'error'),
  })

  const startEditing = (id: number, name: string) => {
    setEditingId(id)
    setEditingName(name)
  }

  const commitRename = (id: number) => {
    const name = editingName.trim()
    if (!name) return
    renameMutation.mutate({ id, name })
  }

  return (
    <Box>
      <Breadcrumbs sx={{ mb: 1.5 }}>
        <MuiLink component="button" type="button" underline="hover" onClick={() => onNavigateBreadcrumb(-1)}>
          Racine
        </MuiLink>
        {breadcrumb.map((crumb, index) => (
          <MuiLink
            key={crumb.id}
            component="button"
            type="button"
            underline="hover"
            onClick={() => onNavigateBreadcrumb(index)}
          >
            {crumb.name}
          </MuiLink>
        ))}
      </Breadcrumbs>

      {isLoading ? (
        <CircularProgress size={16} />
      ) : children.length === 0 ? (
        <Typography variant="body2" color="textSecondary">
          Aucun emplacement enfant.
        </Typography>
      ) : (
        <List disablePadding sx={{ bgcolor: 'background.default', borderRadius: 2 }}>
          {children.map((child) =>
            editingId === child.id ? (
              <Box key={child.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 2, py: 1 }}>
                <TextField
                  size="small"
                  value={editingName}
                  onChange={(event) => setEditingName(event.target.value)}
                  autoFocus
                  fullWidth
                  onKeyDown={(event) => event.key === 'Enter' && commitRename(child.id)}
                  onBlur={() => commitRename(child.id)}
                />
              </Box>
            ) : (
              <ListItemButton key={child.id} onClick={() => onSelect(child.id, child.name)}>
                <ListItemText primary={child.name} />
                <IconButton
                  size="small"
                  aria-label="Renommer"
                  onClick={(event) => {
                    event.stopPropagation()
                    startEditing(child.id, child.name)
                  }}
                >
                  <EditIcon fontSize="small" />
                </IconButton>
                {child.hasChildren && (
                  <Typography color="textDisabled" sx={{ ml: 1 }}>
                    &rarr;
                  </Typography>
                )}
              </ListItemButton>
            ),
          )}
        </List>
      )}
    </Box>
  )
}
