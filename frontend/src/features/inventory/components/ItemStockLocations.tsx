import { useQuery } from '@tanstack/react-query'
import { Box, CircularProgress, Stack, Typography } from '@mui/material'
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
    return <CircularProgress size={16} />
  }

  if (stock.length === 0) {
    return (
      <Typography variant="body2" color="textSecondary">
        Cet objet n'est stocké nulle part.
      </Typography>
    )
  }

  return (
    <Stack spacing={1}>
      {stock.map((entry) => (
        <Box
          key={entry.storageLocationId}
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderRadius: 2,
            bgcolor: 'background.default',
            px: 1.5,
            py: 1,
          }}
        >
          <Typography>{entry.storageLocationName}</Typography>
          <Typography color="primary" sx={{ fontWeight: 700 }}>
            {entry.quantity}
          </Typography>
        </Box>
      ))}
    </Stack>
  )
}
