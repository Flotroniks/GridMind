import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import DeleteOutlineIcon from '@mui/icons-material/Delete'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import LabelOutlinedIcon from '@mui/icons-material/LabelOutlined'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useToast } from '@/components/common/useToast'
import * as categoryApi from '@/features/categories/api/categoryApi'
import type { Category } from '@/features/categories/types/Category'
import { ApiError } from '@/lib/apiClient'

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function CategoryManager() {
  const { showToast } = useToast()
  const [categories, setCategories] = useState<Category[]>([])
  const [error, setError] = useState<string | null>(null)
  const [newName, setNewName] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<Category | null>(null)

  const load = () => {
    categoryApi
      .listCategories()
      .then(setCategories)
      .catch((loadError: unknown) => setError(messageOf(loadError, 'Impossible de charger les catégories.')))
  }

  useEffect(load, [])

  const handleCreate = async () => {
    const name = newName.trim()
    if (!name) return
    try {
      await categoryApi.createCategory(name)
      setNewName('')
      showToast('Catégorie créée.', 'success')
      load()
    } catch (createError) {
      showToast(messageOf(createError, 'Création impossible.'), 'error')
    }
  }

  const startEditing = (category: Category) => {
    setEditingId(category.id)
    setEditingName(category.name)
  }

  const handleRename = async (id: number) => {
    const name = editingName.trim()
    if (!name) return
    try {
      await categoryApi.renameCategory(id, name)
      setEditingId(null)
      showToast('Catégorie renommée.', 'success')
      load()
    } catch (renameError) {
      showToast(messageOf(renameError, 'Renommage impossible.'), 'error')
    }
  }

  const handleDeleteConfirmed = async () => {
    if (!deleteTarget) return
    try {
      await categoryApi.deleteCategory(deleteTarget.id)
      showToast('Catégorie supprimée.', 'success')
      load()
    } catch (deleteError) {
      showToast(messageOf(deleteError, 'Suppression impossible.'), 'error')
    } finally {
      setDeleteTarget(null)
    }
  }

  return (
    <Stack spacing={2.5}>
      <Typography variant="body2" color="textSecondary">
        Supprimer une catégorie ne supprime aucun objet — les objets qui l'utilisaient redeviennent
        simplement sans catégorie.
      </Typography>

      {error && <Alert severity="error">{error}</Alert>}

      <Stack direction="row" spacing={1}>
        <TextField
          size="small"
          placeholder="Nouvelle catégorie…"
          value={newName}
          onChange={(event) => setNewName(event.target.value)}
          autoComplete="off"
          fullWidth
          onKeyDown={(event) => event.key === 'Enter' && void handleCreate()}
        />
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => void handleCreate()}
          sx={{ whiteSpace: 'nowrap', flexShrink: 0 }}
        >
          Ajouter
        </Button>
      </Stack>

      {categories.length === 0 ? (
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
          <LabelOutlinedIcon sx={{ fontSize: 32, color: 'text.disabled' }} />
          <Typography variant="body2" color="textSecondary">
            Aucune catégorie pour l'instant.
          </Typography>
        </Stack>
      ) : (
        <List
          disablePadding
          sx={{ bgcolor: 'background.default', borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}
        >
          {categories.map((category, index) => (
            <ListItem
              key={category.id}
              sx={{ py: 1, gap: 1, borderTop: index > 0 ? '1px solid' : 'none', borderColor: 'divider' }}
            >
              {editingId === category.id ? (
                <TextField
                  size="small"
                  value={editingName}
                  onChange={(event) => setEditingName(event.target.value)}
                  autoFocus
                  fullWidth
                  onKeyDown={(event) => event.key === 'Enter' && void handleRename(category.id)}
                  onBlur={() => void handleRename(category.id)}
                />
              ) : (
                <>
                  <ListItemIcon sx={{ minWidth: 36 }}>
                    <LabelOutlinedIcon sx={{ fontSize: 20, color: 'text.disabled' }} />
                  </ListItemIcon>
                  <Typography sx={{ flexGrow: 1 }}>{category.name}</Typography>
                </>
              )}
              <IconButton size="small" onClick={() => startEditing(category)} aria-label="Renommer" sx={{ color: 'text.disabled' }}>
                <EditOutlinedIcon fontSize="small" />
              </IconButton>
              <IconButton size="small" onClick={() => setDeleteTarget(category)} aria-label="Supprimer" sx={{ color: 'text.disabled' }}>
                <DeleteOutlineIcon fontSize="small" />
              </IconButton>
            </ListItem>
          ))}
        </List>
      )}

      <Dialog open={deleteTarget !== null} onClose={() => setDeleteTarget(null)}>
        <DialogTitle>Supprimer la catégorie</DialogTitle>
        <DialogContent>
          <Typography>
            Confirmer la suppression de <strong>{deleteTarget?.name}</strong> ? Les objets qui
            l'utilisaient redeviendront sans catégorie.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteTarget(null)}>Annuler</Button>
          <Button color="error" variant="contained" onClick={() => void handleDeleteConfirmed()}>
            Supprimer
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  )
}
