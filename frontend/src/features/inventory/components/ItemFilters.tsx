import { Box, IconButton, InputAdornment, MenuItem, TextField } from '@mui/material'
import { alpha } from '@mui/material/styles'
import SearchIcon from '@mui/icons-material/Search'
import ClearIcon from '@mui/icons-material/Clear'
import type { Category } from '@/features/categories/types/Category'
import type { ItemFilters as ItemFiltersValue } from '../types/Item'

interface ItemFiltersProps {
  value: ItemFiltersValue
  categories: Category[]
  onChange: (value: ItemFiltersValue) => void
}

export function ItemFilters({ value, categories, onChange }: ItemFiltersProps) {
  const search = value.search ?? ''

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      <TextField
        placeholder="Rechercher un objet, une référence, un tag…"
        value={search}
        onChange={(event) => onChange({ ...value, search: event.target.value })}
        autoComplete="off"
        fullWidth
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon color="primary" sx={{ fontSize: 26, ml: 0.5 }} />
              </InputAdornment>
            ),
            endAdornment: search ? (
              <InputAdornment position="end">
                <IconButton
                  size="small"
                  onClick={() => onChange({ ...value, search: '' })}
                  aria-label="Effacer la recherche"
                >
                  <ClearIcon fontSize="small" />
                </IconButton>
              </InputAdornment>
            ) : undefined,
          },
        }}
        sx={{
          '& .MuiOutlinedInput-root': {
            fontSize: { xs: '1.1rem', sm: '1.35rem' },
            borderRadius: 999,
            py: 0.75,
            px: 1,
            bgcolor: (theme) => alpha(theme.palette.primary.main, theme.palette.mode === 'dark' ? 0.16 : 0.06),
            transition: 'background-color .2s ease, box-shadow .2s ease',
            '& fieldset': { border: 'none' },
            '&:hover': {
              bgcolor: (theme) => alpha(theme.palette.primary.main, theme.palette.mode === 'dark' ? 0.22 : 0.09),
            },
            '&.Mui-focused': {
              bgcolor: 'background.paper',
              boxShadow: (theme) => `0 0 0 3px ${alpha(theme.palette.primary.main, 0.35)}`,
            },
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
