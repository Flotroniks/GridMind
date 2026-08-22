import { useState } from 'react'
import { CreateLocationForm } from '../components/CreateLocationForm'
import { LocationContents } from '../components/LocationContents'
import { LocationTree } from '../components/LocationTree'
import { MoveStockDialog } from '../components/MoveStockDialog'

export function StorageHierarchyPage() {
  const [breadcrumb, setBreadcrumb] = useState<{ id: number; name: string }[]>([])
  const [movingStock, setMovingStock] = useState(false)

  const currentId = breadcrumb.length > 0 ? breadcrumb[breadcrumb.length - 1].id : null

  const handleSelect = (id: number, name: string) => {
    setBreadcrumb((current) => [...current, { id, name }])
  }

  const handleNavigateBreadcrumb = (index: number) => {
    setBreadcrumb((current) => (index < 0 ? [] : current.slice(0, index + 1)))
  }

  return (
    <div className="mx-auto w-full max-w-4xl">
      <div className="card bg-base-200/80 shadow-xl backdrop-blur">
        <div className="card-body">
          <div className="mb-4">
            <p className="text-xs font-semibold tracking-widest text-primary uppercase">
              GridMind
            </p>
            <h1 className="text-3xl font-bold sm:text-4xl">Stockage</h1>
          </div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <LocationTree
                parentId={currentId}
                breadcrumb={breadcrumb}
                onSelect={handleSelect}
                onNavigateBreadcrumb={handleNavigateBreadcrumb}
              />
              <div className="mt-4">
                <CreateLocationForm parentId={currentId} />
              </div>
            </div>

            <div>
              <div className="mb-2 flex items-center justify-between">
                <h2 className="font-semibold">Contenu</h2>
                {currentId != null && (
                  <button
                    type="button"
                    className="btn btn-xs"
                    onClick={() => setMovingStock(true)}
                  >
                    Déplacer du stock
                  </button>
                )}
              </div>
              <LocationContents locationId={currentId} />
            </div>
          </div>
        </div>
      </div>

      <MoveStockDialog
        open={movingStock}
        fromLocationId={currentId}
        onClose={() => setMovingStock(false)}
      />
    </div>
  )
}
