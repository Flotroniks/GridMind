import { useCallback, useEffect, useState } from 'react'
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
    <div className="mx-auto w-full max-w-5xl">
      <div className="card bg-base-200/80 shadow-xl backdrop-blur">
        <div className="card-body">
          <div className="mb-6 flex flex-col items-stretch justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <p className="text-xs font-semibold tracking-widest text-primary uppercase">
                GridMind
              </p>
              <h1 className="text-3xl font-bold sm:text-4xl">Inventaire</h1>
            </div>
            <button type="button" className="btn btn-primary" onClick={() => setCreating(true)}>
              Ajouter un objet
            </button>
          </div>

          <div className="mb-6">
            <ItemFilters value={filters} categories={categories} onChange={setFilters} />
          </div>

          {error && (
            <div role="alert" className="alert alert-error mb-5">
              <span>{error}</span>
            </div>
          )}

          {loading ? (
            <p className="flex items-center gap-2 text-base-content/70">
              <span className="loading loading-spinner loading-sm" />
              Chargement de l'inventaire…
            </p>
          ) : (
            <ItemList
              items={items}
              onEditRequest={setEditingItem}
              onDeleteRequest={setItemPendingDelete}
            />
          )}
        </div>
      </div>

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
    </div>
  )
}
