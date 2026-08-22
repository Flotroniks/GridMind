import AddIcon from '@mui/icons-material/Add'
import { Box, Button, Typography } from '@mui/material'
import type { Item } from '../types/Item'
import { ItemRow } from './ItemRow'

interface ItemListProps {
  items: Item[]
  onEditRequest: (item: Item) => void
  onDeleteRequest: (item: Item) => void
  hasActiveFilters: boolean
  onCreateRequest: () => void
}

export function ItemList({
  items,
  onEditRequest,
  onDeleteRequest,
  hasActiveFilters,
  onCreateRequest,
}: ItemListProps) {
  if (items.length === 0) {
    return (
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 1.5,
          py: 6,
          px: 2,
          textAlign: 'center',
          border: '1px dashed',
          borderColor: 'divider',
          borderRadius: 2,
        }}
      >
        <Typography color="textSecondary">
          {hasActiveFilters ? 'Aucun objet ne correspond à cette recherche.' : "Aucun objet pour l'instant."}
        </Typography>
        <Button variant="outlined" startIcon={<AddIcon />} onClick={onCreateRequest}>
          {hasActiveFilters ? 'Créer cet objet' : 'Ajouter un objet'}
        </Button>
      </Box>
    )
  }

  return (
    <Box
      component="ul"
      sx={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
        gap: 2,
        p: 0,
        m: 0,
      }}
    >
      {items.map((item) => (
        <ItemRow
          key={item.id}
          item={item}
          onEditRequest={onEditRequest}
          onDeleteRequest={onDeleteRequest}
        />
      ))}
    </Box>
  )
}
