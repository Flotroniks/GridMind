import { Box, Button, Card, CardContent, Chip, Stack, Typography } from '@mui/material'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import { Link } from 'react-router'
import type { Item } from '../types/Item'
import { TagBadgeList } from './TagBadgeList'

interface ItemRowProps {
  item: Item
  onEditRequest: (item: Item) => void
  onDeleteRequest: (item: Item) => void
}

export function ItemRow({ item, onEditRequest, onDeleteRequest }: ItemRowProps) {
  const isLowStock = item.quantityAvailable <= item.minimumQuantity

  return (
    <Card component="li" sx={{ listStyle: 'none', overflow: 'hidden' }}>
      <Box
        component={Link}
        to={`/inventory/${item.id}`}
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          aspectRatio: '4 / 3',
          bgcolor: 'action.hover',
          color: 'text.disabled',
        }}
      >
        <ImageOutlinedIcon sx={{ fontSize: 48 }} />
      </Box>

      <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2 }}>
          <Box>
            <Typography
              component={Link}
              to={`/inventory/${item.id}`}
              variant="h6"
              sx={{ color: 'inherit', textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}
            >
              {item.name}
            </Typography>
            {item.manufacturer && (
              <Typography variant="body2" color="textSecondary">
                {item.manufacturer}
              </Typography>
            )}
          </Box>
          <Typography
            variant="h4"
            sx={{ fontWeight: 700, color: isLowStock ? 'error.main' : 'primary.main' }}
          >
            {item.quantityAvailable}
          </Typography>
        </Box>

        {item.categoryName && (
          <Chip
            label={item.categoryName}
            size="small"
            color="primary"
            variant="outlined"
            sx={{ width: 'fit-content' }}
          />
        )}

        <TagBadgeList tags={item.tags} />

        {(item.quantityInUse > 0 || item.quantityHs > 0) && (
          <Typography variant="caption" color="textSecondary">
            {item.quantity} au total
            {item.quantityInUse > 0 ? ` · ${item.quantityInUse} en utilisation` : ''}
            {item.quantityHs > 0 ? ` · ${item.quantityHs} HS` : ''}
          </Typography>
        )}

        {isLowStock && (
          <Typography variant="caption" color="error">
            Stock disponible sous le seuil minimal ({item.minimumQuantity})
          </Typography>
        )}

        <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
          <Button size="small" onClick={() => onEditRequest(item)}>
            Modifier
          </Button>
          <Button size="small" color="error" onClick={() => onDeleteRequest(item)}>
            Supprimer
          </Button>
        </Stack>
      </CardContent>
    </Card>
  )
}
