import type { ReactNode } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import PlaceOutlinedIcon from '@mui/icons-material/PlaceOutlined'
import { Chip, List, ListItemButton, ListItemIcon, ListItemText, Stack, Typography } from '@mui/material'
import * as storageApi from '../api/storageApi'

interface LocationContentsProps {
  locationId: number | null
}

function EmptyState({ icon, message }: { icon: ReactNode; message: string }) {
  return (
    <Stack
      spacing={1}
      sx={{
        alignItems: 'center',
        py: 4,
        px: 2,
        textAlign: 'center',
        border: '1px dashed',
        borderColor: 'divider',
        borderRadius: 2,
      }}
    >
      {icon}
      <Typography variant="body2" color="textSecondary">
        {message}
      </Typography>
    </Stack>
  )
}

export function LocationContents({ locationId }: LocationContentsProps) {
  const { data: contents = [], isLoading } = useQuery({
    queryKey: ['storage', 'contents', locationId],
    queryFn: () => storageApi.getLocationContents(locationId!),
    enabled: locationId != null,
  })

  if (locationId == null) {
    return (
      <EmptyState
        icon={<PlaceOutlinedIcon sx={{ fontSize: 32, color: 'text.disabled' }} />}
        message="Sélectionnez un emplacement."
      />
    )
  }

  if (isLoading) {
    return null
  }

  if (contents.length === 0) {
    return (
      <EmptyState
        icon={<Inventory2OutlinedIcon sx={{ fontSize: 32, color: 'text.disabled' }} />}
        message="Aucun objet stocké ici."
      />
    )
  }

  return (
    <List
      disablePadding
      sx={{ bgcolor: 'background.default', borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}
    >
      {contents.map((stock, index) => (
        <ListItemButton
          key={stock.itemId}
          component={Link}
          to={`/inventory/${stock.itemId}`}
          sx={{ py: 1.25, borderTop: index > 0 ? '1px solid' : 'none', borderColor: 'divider' }}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <Inventory2OutlinedIcon sx={{ fontSize: 20, color: 'text.disabled' }} />
          </ListItemIcon>
          <ListItemText primary={stock.itemName} />
          <Chip label={stock.quantity} size="small" color="primary" />
        </ListItemButton>
      ))}
    </List>
  )
}
