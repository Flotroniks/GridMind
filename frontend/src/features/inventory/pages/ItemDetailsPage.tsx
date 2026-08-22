import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { ApiError } from '@/lib/apiClient'
import * as inventoryApi from '../api/inventoryApi'
import { ItemStockLocations } from '../components/ItemStockLocations'
import { TagBadgeList } from '../components/TagBadgeList'
import type { Item } from '../types/Item'

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function ItemDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const [item, setItem] = useState<Item | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    inventoryApi
      .getItem(Number(id))
      .then(setItem)
      .catch((loadError: unknown) => setError(messageOf(loadError, "Impossible de charger l'objet.")))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <p className="flex items-center gap-2 text-base-content/70">
        <span className="loading loading-spinner loading-sm" />
        Chargement…
      </p>
    )
  }

  if (error || !item) {
    return (
      <div role="alert" className="alert alert-error mx-auto w-full max-w-2xl">
        <span>{error ?? 'Objet introuvable.'}</span>
      </div>
    )
  }

  return (
    <div className="mx-auto w-full max-w-2xl">
      <Link to="/" className="link link-hover mb-4 inline-block text-sm">
        &larr; Retour à l'inventaire
      </Link>

      <div className="card bg-base-200/80 shadow-xl backdrop-blur">
        <div className="card-body gap-3">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold">{item.name}</h1>
              {item.manufacturer && (
                <p className="text-sm text-base-content/60">
                  {item.manufacturer}
                  {item.reference ? ` · ${item.reference}` : ''}
                </p>
              )}
            </div>
            <span className="text-3xl font-bold text-primary">{item.quantity}</span>
          </div>

          {item.categoryName && (
            <span className="badge badge-primary badge-outline w-fit">{item.categoryName}</span>
          )}

          <TagBadgeList tags={item.tags} />

          {item.description && <p className="text-base-content/80">{item.description}</p>}

          <div className="grid grid-cols-1 gap-3 text-sm sm:grid-cols-2">
            <p>
              <span className="text-base-content/60">Quantité minimale : </span>
              {item.minimumQuantity}
            </p>
            {item.productUrl && (
              <p>
                <a href={item.productUrl} target="_blank" rel="noreferrer" className="link">
                  Page produit
                </a>
              </p>
            )}
            {item.datasheetUrl && (
              <p>
                <a href={item.datasheetUrl} target="_blank" rel="noreferrer" className="link">
                  Datasheet
                </a>
              </p>
            )}
          </div>

          {item.notes && (
            <div>
              <p className="text-sm text-base-content/60">Notes</p>
              <p>{item.notes}</p>
            </div>
          )}

          <div>
            <p className="mb-2 text-sm text-base-content/60">Emplacements de stockage</p>
            <ItemStockLocations itemId={item.id} />
          </div>
        </div>
      </div>
    </div>
  )
}
