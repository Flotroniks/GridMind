import { Box, MenuItem, TextField } from '@mui/material'
import type { Category } from '@/features/categories/types/Category'
import type { ItemFilters as ItemFiltersValue } from '../types/Item'

interface ItemFiltersProps {
  value: ItemFiltersValue
  categories: Category[]
  onChange: (value: ItemFiltersValue) => void
}

export function ItemFilters({ value, categories, onChange }: ItemFiltersProps) {
  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' }, gap: 1.5 }}>
      <TextField
        size="small"
        placeholder="Rechercher…"
        value={value.search ?? ''}
        onChange={(event) => onChange({ ...value, search: event.target.value })}
        autoComplete="off"
      />
      <TextField
        select
        size="small"
        value={value.categoryId ?? ''}
        onChange={(event) =>
          onChange({
            ...value,
            categoryId: event.target.value ? Number(event.target.value) : undefined,
          })
        }
      >
        <MenuItem value="">Toutes les catégories</MenuItem>
        {categories.map((category) => (
          <MenuItem key={category.id} value={category.id}>
            {category.name}
          </MenuItem>
        ))}
      </TextField>
      <TextField
        size="small"
        placeholder="Fabricant…"
        value={value.manufacturer ?? ''}
        onChange={(event) => onChange({ ...value, manufacturer: event.target.value })}
        autoComplete="off"
      />
    </Box>
  )
}
