import { useQuery } from '@tanstack/react-query'
import { Box, CircularProgress, Stack, Typography } from '@mui/material'
import * as storageApi from '../api/storageApi'

interface LocationContentsProps {
  locationId: number | null
}

export function LocationContents({ locationId }: LocationContentsProps) {
  const { data: contents = [], isLoading } = useQuery({
    queryKey: ['storage', 'contents', locationId],
    queryFn: () => storageApi.getLocationContents(locationId!),
    enabled: locationId != null,
  })

  if (locationId == null) {
    return (
      <Typography variant="body2" color="textSecondary">
        Sélectionnez un emplacement.
      </Typography>
    )
  }

  if (isLoading) {
    return <CircularProgress size={16} />
  }

  if (contents.length === 0) {
    return (
      <Typography variant="body2" color="textSecondary">
        Aucun objet stocké ici.
      </Typography>
    )
  }

  return (
    <Stack spacing={1}>
      {contents.map((stock) => (
        <Box
          key={stock.itemId}
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
          <Typography>{stock.itemName}</Typography>
          <Typography color="primary" sx={{ fontWeight: 700 }}>
            {stock.quantity}
          </Typography>
        </Box>
      ))}
    </Stack>
  )
}
