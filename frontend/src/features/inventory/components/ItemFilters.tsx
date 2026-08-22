import { Box, InputAdornment, MenuItem, TextField } from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import type { Category } from '@/features/categories/types/Category'
import type { ItemFilters as ItemFiltersValue } from '../types/Item'

interface ItemFiltersProps {
  value: ItemFiltersValue
  categories: Category[]
  onChange: (value: ItemFiltersValue) => void
}

export function ItemFilters({ value, categories, onChange }: ItemFiltersProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      <TextField
        placeholder="Rechercher un objet, une référence, un tag…"
        value={value.search ?? ''}
        onChange={(event) => onChange({ ...value, search: event.target.value })}
        autoComplete="off"
        fullWidth
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon color="action" />
              </InputAdornment>
            ),
          },
        }}
        sx={{
          '& .MuiOutlinedInput-root': {
            fontSize: '1.25rem',
            py: 0.5,
          },
        }}
      />

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 240px))' }, gap: 1.5 }}>
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
    </Box>
  )
}
