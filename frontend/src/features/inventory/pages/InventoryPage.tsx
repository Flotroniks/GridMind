import { useCallback, useEffect, useState } from 'react'
import { Alert, Box, Button, Card, CardContent, CircularProgress, Stack, Typography } from '@mui/material'
import { useToast } from '@/components/common/useToast'
import * as categoryApi from '@/features/categories/api/categoryApi'
import type { Category } from '@/features/categories/types/Category'
import { ApiError } from '@/lib/apiClient'
import * as inventoryApi from '../api/inventoryApi'
import { DeleteItemDialog } from '../components/DeleteItemDialog'
import { ItemFilters } from '../components/ItemFilters'
import { ItemFormModal } from '../components/ItemFormModal'
import { ItemList } from '../components/ItemList'
import type { Item, ItemFilters as ItemFiltersValue, ItemInput } from '../types/Item'

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

function toItemInput(item: Item): ItemInput {
  return {
    name: item.name,
    quantity: item.quantity,
    description: item.description,
    manufacturer: item.manufacturer,
    reference: item.reference,
    categoryId: item.categoryId,
    tags: item.tags,
    notes: item.notes,
    productUrl: item.productUrl,
    datasheetUrl: item.datasheetUrl,
    minimumQuantity: item.minimumQuantity,
    status: item.status,
  }
}

export function InventoryPage() {
  const { showToast } = useToast()
  const [items, setItems] = useState<Item[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [filters, setFilters] = useState<ItemFiltersValue>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [itemPendingDelete, setItemPendingDelete] = useState<Item | null>(null)
  const [editingItem, setEditingItem] = useState<Item | null>(null)
  const [creating, setCreating] = useState(false)

  const loadItems = useCallback(async (currentFilters: ItemFiltersValue) => {
    try {
      setLoading(true)
      const data = await inventoryApi.listItems(currentFilters)
      setItems(data)
      setError(null)
    } catch (loadError) {
      setError(messageOf(loadError, "Impossible de charger l'inventaire."))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    categoryApi
      .listCategories()
      .then(setCategories)
      .catch((loadError: unknown) =>
        showToast(messageOf(loadError, 'Impossible de charger les catégories.'), 'error'),
      )
  }, [showToast])

  useEffect(() => {
    const timeout = setTimeout(() => void loadItems(filters), 250)
    return () => clearTimeout(timeout)
  }, [filters, loadItems])

  const handleCreateCategory = async (name: string): Promise<Category> => {
    const category = await categoryApi.createCategory(name)
    setCategories((current) => [...current, category])
    return category
  }

  const handleCreate = async (input: ItemInput) => {
    try {
      await inventoryApi.createItem(input)
      showToast('Objet ajouté.', 'success')
      await loadItems(filters)
    } catch (createError) {
      showToast(messageOf(createError, 'Création impossible.'), 'error')
    }
  }

  const handleUpdate = async (input: ItemInput) => {
    if (!editingItem) return
    try {
      await inventoryApi.updateItem(editingItem.id, input)
      showToast('Objet mis à jour.', 'success')
      await loadItems(filters)
    } catch (updateError) {
      showToast(messageOf(updateError, 'Mise à jour impossible.'), 'error')
    }
  }

  const handleDeleteConfirmed = async () => {
    if (!itemPendingDelete) return
    try {
      await inventoryApi.deleteItem(itemPendingDelete.id)
      showToast('Objet supprimé.', 'success')
      await loadItems(filters)
    } catch (deleteError) {
      showToast(messageOf(deleteError, 'Suppression impossible.'), 'error')
    } finally {
      setItemPendingDelete(null)
    }
  }

  return (
    <Box sx={{ mx: 'auto', width: { xs: '100%', sm: '75vw' }, maxWidth: 1600 }}>
      <Card sx={{ backdropFilter: 'blur(8px)' }}>
        <CardContent sx={{ py: 3, px: { xs: 3, sm: 5, md: 8 } }}>
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            sx={{
              mb: 3,
              justifyContent: 'space-between',
              alignItems: { xs: 'stretch', sm: 'center' },
            }}
          >
            <Box>
              <Typography variant="overline" color="primary" sx={{ fontWeight: 600, letterSpacing: 2 }}>
                GridMind
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                Inventaire
              </Typography>
            </Box>
            <Button variant="contained" onClick={() => setCreating(true)}>
              Ajouter un objet
            </Button>
          </Stack>

          <Box sx={{ mb: 3 }}>
            <ItemFilters value={filters} categories={categories} onChange={setFilters} />
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2.5 }}>
              {error}
            </Alert>
          )}

          {loading ? (
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: 'text.secondary' }}>
              <CircularProgress size={16} />
              <Typography color="textSecondary">Chargement de l'inventaire…</Typography>
            </Stack>
          ) : (
            <ItemList
              items={items}
              onEditRequest={setEditingItem}
              onDeleteRequest={setItemPendingDelete}
            />
          )}
        </CardContent>
      </Card>

      <ItemFormModal
        open={creating}
        title="Ajouter un objet"
        categories={categories}
        submitLabel="Ajouter"
        onClose={() => setCreating(false)}
        onSubmit={handleCreate}
        onCreateCategory={handleCreateCategory}
      />

      <ItemFormModal
        open={editingItem !== null}
        title="Modifier l'objet"
        initialValue={editingItem ? toItemInput(editingItem) : undefined}
        categories={categories}
        submitLabel="Enregistrer"
        onClose={() => setEditingItem(null)}
        onSubmit={handleUpdate}
        onCreateCategory={handleCreateCategory}
      />

      <DeleteItemDialog
        item={itemPendingDelete}
        onConfirm={() => void handleDeleteConfirmed()}
        onCancel={() => setItemPendingDelete(null)}
      />
    </Box>
  )
}
