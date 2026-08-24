import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined'
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined'
import {
  Box,
  Breadcrumbs,
  CircularProgress,
  IconButton,
  Link as MuiLink,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Stack,
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
      <Breadcrumbs
        separator={<ChevronRightIcon sx={{ fontSize: 16, color: 'text.disabled' }} />}
        sx={{ mb: 2 }}
      >
        <MuiLink
          component="button"
          type="button"
          underline="hover"
          onClick={() => onNavigateBreadcrumb(-1)}
          sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5 }}
        >
          <HomeOutlinedIcon sx={{ fontSize: 17 }} />
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
        <Stack sx={{ py: 4, alignItems: 'center' }}>
          <CircularProgress size={20} />
        </Stack>
      ) : children.length === 0 ? (
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
          <FolderOutlinedIcon sx={{ fontSize: 32, color: 'text.disabled' }} />
          <Typography variant="body2" color="textSecondary">
            Aucun emplacement enfant.
          </Typography>
        </Stack>
      ) : (
        <List
          disablePadding
          sx={{ bgcolor: 'background.default', borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}
        >
          {children.map((child, index) =>
            editingId === child.id ? (
              <Box
                key={child.id}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                  px: 2,
                  py: 1,
                  borderTop: index > 0 ? '1px solid' : 'none',
                  borderColor: 'divider',
                }}
              >
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
              <ListItemButton
                key={child.id}
                onClick={() => onSelect(child.id, child.name)}
                sx={{
                  py: 1.25,
                  borderTop: index > 0 ? '1px solid' : 'none',
                  borderColor: 'divider',
                }}
              >
                <ListItemIcon sx={{ minWidth: 36 }}>
                  <FolderOutlinedIcon
                    sx={{ fontSize: 20, color: child.hasChildren ? 'primary.main' : 'text.disabled' }}
                  />
                </ListItemIcon>
                <ListItemText primary={child.name} />
                <IconButton
                  size="small"
                  aria-label="Renommer"
                  sx={{ color: 'text.disabled', mr: child.hasChildren ? 0.5 : 0 }}
                  onClick={(event) => {
                    event.stopPropagation()
                    startEditing(child.id, child.name)
                  }}
                >
                  <EditOutlinedIcon fontSize="small" />
                </IconButton>
                {child.hasChildren && <ChevronRightIcon sx={{ color: 'text.disabled' }} />}
              </ListItemButton>
            ),
          )}
        </List>
      )}
    </Box>
  )
}
