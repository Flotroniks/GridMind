import type { Item } from '../types/Item'
import { ItemRow } from './ItemRow'

interface ItemListProps {
  items: Item[]
  onEditRequest: (item: Item) => void
  onDeleteRequest: (item: Item) => void
}

export function ItemList({ items, onEditRequest, onDeleteRequest }: ItemListProps) {
  if (items.length === 0) {
    return <p className="text-base-content/70">Aucun objet pour l'instant.</p>
  }

  return (
    <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3">
      {items.map((item) => (
        <ItemRow
          key={item.id}
          item={item}
          onEditRequest={onEditRequest}
          onDeleteRequest={onDeleteRequest}
        />
      ))}
    </ul>
  )
}
