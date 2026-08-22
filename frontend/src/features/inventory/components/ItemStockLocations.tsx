import { useQuery } from '@tanstack/react-query'
import * as storageApi from '@/features/storage/api/storageApi'

interface ItemStockLocationsProps {
  itemId: number
}

export function ItemStockLocations({ itemId }: ItemStockLocationsProps) {
  const { data: stock = [], isLoading } = useQuery({
    queryKey: ['storage', 'itemStock', itemId],
    queryFn: () => storageApi.getItemStock(itemId),
  })

  if (isLoading) {
    return <span className="loading loading-spinner loading-sm" />
  }

  if (stock.length === 0) {
    return <p className="text-sm text-base-content/60">Cet objet n'est stocké nulle part.</p>
  }

  return (
    <ul className="flex flex-col gap-2">
      {stock.map((entry) => (
        <li
          key={entry.storageLocationId}
          className="flex items-center justify-between rounded-lg bg-base-100 px-3 py-2"
        >
          <span>{entry.storageLocationName}</span>
          <span className="font-bold text-primary">{entry.quantity}</span>
        </li>
      ))}
    </ul>
  )
}
