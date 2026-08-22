import type { Category } from '@/features/categories/types/Category'
import type { ItemFilters as ItemFiltersValue } from '../types/Item'

interface ItemFiltersProps {
  value: ItemFiltersValue
  categories: Category[]
  onChange: (value: ItemFiltersValue) => void
}

export function ItemFilters({ value, categories, onChange }: ItemFiltersProps) {
  return (
    <div className="@container">
      <div className="grid grid-cols-1 gap-3 @sm:grid-cols-3">
        <input
          type="text"
          className="input input-bordered input-sm w-full"
          placeholder="Rechercher…"
          value={value.search ?? ''}
          onChange={(event) => onChange({ ...value, search: event.target.value })}
          autoComplete="off"
        />
        <select
          className="select select-bordered select-sm w-full"
          value={value.categoryId ?? ''}
          onChange={(event) =>
            onChange({
              ...value,
              categoryId: event.target.value ? Number(event.target.value) : undefined,
            })
          }
        >
          <option value="">Toutes les catégories</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        <input
          type="text"
          className="input input-bordered input-sm w-full"
          placeholder="Fabricant…"
          value={value.manufacturer ?? ''}
          onChange={(event) => onChange({ ...value, manufacturer: event.target.value })}
          autoComplete="off"
        />
      </div>
    </div>
  )
}
