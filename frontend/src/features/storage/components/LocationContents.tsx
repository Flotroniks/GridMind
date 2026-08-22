import { useQuery } from '@tanstack/react-query'
import * as storageApi from '../api/storageApi'

interface LocationContentsProps {
  locationId: number | null
}

export function LocationContents({ locationId }: LocationContentsProps) {
  const { data: contents = [], isLoading } = useQuery({
    queryKey: ['storage', 'contents', locationId],
    queryFn: () => storageApi.getLocationContents(locationId!),
    enabled: locationId != null,
  })

  if (locationId == null) {
    return <p className="text-sm text-base-content/60">Sélectionnez un emplacement.</p>
  }

  if (isLoading) {
    return <span className="loading loading-spinner loading-sm" />
  }

  if (contents.length === 0) {
    return <p className="text-sm text-base-content/60">Aucun objet stocké ici.</p>
  }

  return (
    <ul className="flex flex-col gap-2">
      {contents.map((stock) => (
        <li
          key={stock.itemId}
          className="flex items-center justify-between rounded-lg bg-base-100 px-3 py-2"
        >
          <span>{stock.itemName}</span>
          <span className="font-bold text-primary">{stock.quantity}</span>
        </li>
      ))}
    </ul>
  )
}
