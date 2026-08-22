import { useQuery } from '@tanstack/react-query'
import * as storageApi from '../api/storageApi'

interface LocationTreeProps {
  parentId: number | null
  breadcrumb: { id: number; name: string }[]
  onSelect: (id: number, name: string) => void
  onNavigateBreadcrumb: (index: number) => void
}

export function LocationTree({
  parentId,
  breadcrumb,
  onSelect,
  onNavigateBreadcrumb,
}: LocationTreeProps) {
  const { data: children = [], isLoading } = useQuery({
    queryKey: ['storage', 'children', parentId],
    queryFn: () => storageApi.listLocations(parentId),
  })

  return (
    <div>
      <div className="breadcrumbs mb-3 text-sm">
        <ul>
          <li>
            <button type="button" className="link link-hover" onClick={() => onNavigateBreadcrumb(-1)}>
              Racine
            </button>
          </li>
          {breadcrumb.map((crumb, index) => (
            <li key={crumb.id}>
              <button
                type="button"
                className="link link-hover"
                onClick={() => onNavigateBreadcrumb(index)}
              >
                {crumb.name}
              </button>
            </li>
          ))}
        </ul>
      </div>

      {isLoading ? (
        <span className="loading loading-spinner loading-sm" />
      ) : children.length === 0 ? (
        <p className="text-sm text-base-content/60">Aucun emplacement enfant.</p>
      ) : (
        <ul className="menu bg-base-100 rounded-box w-full">
          {children.map((child) => (
            <li key={child.id}>
              <button type="button" onClick={() => onSelect(child.id, child.name)}>
                {child.name}
                {child.hasChildren && <span className="text-base-content/40">&rarr;</span>}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
