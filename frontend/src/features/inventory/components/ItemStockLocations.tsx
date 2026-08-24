import { useQuery } from '@tanstack/react-query'
import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined'
import { Chip, List, ListItem, ListItemIcon, ListItemText, Stack, Typography } from '@mui/material'
import * as storageApi from '@/features/storage/api/storageApi'

interface ItemStockLocationsProps {
  itemId: number
}

export function ItemStockLocations({ itemId }: ItemStockLocationsProps) {
  const { data: stock = [], isLoading } = useQuery({
    queryKey: ['storage', 'itemStock', itemId],
    queryFn: () => storageApi.getItemStock(itemId),
  })

  if (isLoading) {
    return null
  }

  if (stock.length === 0) {
    return (
      <Stack
        spacing={1}
        sx={{
          alignItems: 'center',
          py: 3,
          px: 2,
          textAlign: 'center',
          border: '1px dashed',
          borderColor: 'divider',
          borderRadius: 2,
        }}
      >
        <FolderOutlinedIcon sx={{ fontSize: 28, color: 'text.disabled' }} />
        <Typography variant="body2" color="textSecondary">
          Cet objet n'est stocké nulle part.
        </Typography>
      </Stack>
    )
  }

  return (
    <List
      disablePadding
      sx={{ bgcolor: 'background.default', borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}
    >
      {stock.map((entry, index) => (
        <ListItem
          key={entry.storageLocationId}
          sx={{ py: 1, borderTop: index > 0 ? '1px solid' : 'none', borderColor: 'divider' }}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <FolderOutlinedIcon sx={{ fontSize: 20, color: 'text.disabled' }} />
          </ListItemIcon>
          <ListItemText primary={entry.storageLocationName} />
          <Chip label={entry.quantity} size="small" color="primary" />
        </ListItem>
      ))}
    </List>
  )
}
