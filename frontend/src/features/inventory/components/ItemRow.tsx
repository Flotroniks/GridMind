import { Link } from 'react-router'
import type { Item } from '../types/Item'
import { TagBadgeList } from './TagBadgeList'

interface ItemRowProps {
  item: Item
  onEditRequest: (item: Item) => void
  onDeleteRequest: (item: Item) => void
}

export function ItemRow({ item, onEditRequest, onDeleteRequest }: ItemRowProps) {
  const isLowStock = item.quantity <= item.minimumQuantity

  return (
    <li className="card bg-base-100 shadow-sm">
      <div className="card-body gap-2 p-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <Link to={`/inventory/${item.id}`} className="link link-hover">
              <h2 className="card-title text-lg">{item.name}</h2>
            </Link>
            {item.manufacturer && (
              <p className="text-sm text-base-content/60">{item.manufacturer}</p>
            )}
          </div>
          <span className={`text-3xl font-bold ${isLowStock ? 'text-error' : 'text-primary'}`}>
            {item.quantity}
          </span>
        </div>

        {item.categoryName && (
          <span className="badge badge-sm badge-primary badge-outline w-fit">
            {item.categoryName}
          </span>
        )}

        <TagBadgeList tags={item.tags} />

        {isLowStock && (
          <p className="text-xs text-error">Stock sous le seuil minimal ({item.minimumQuantity})</p>
        )}

        <div className="mt-2 flex gap-2">
          <button type="button" className="btn btn-sm btn-ghost" onClick={() => onEditRequest(item)}>
            Modifier
          </button>
          <button
            type="button"
            className="btn btn-sm btn-ghost text-error"
            onClick={() => onDeleteRequest(item)}
          >
            Supprimer
          </button>
        </div>
      </div>
    </li>
  )
}
