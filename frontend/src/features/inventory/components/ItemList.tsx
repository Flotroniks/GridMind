import { Box, Typography } from '@mui/material'
import type { Item } from '../types/Item'
import { ItemRow } from './ItemRow'

interface ItemListProps {
  items: Item[]
  onEditRequest: (item: Item) => void
  onDeleteRequest: (item: Item) => void
}

export function ItemList({ items, onEditRequest, onDeleteRequest }: ItemListProps) {
  if (items.length === 0) {
    return <Typography color="textSecondary">Aucun objet pour l'instant.</Typography>
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
