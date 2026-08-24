import { Chip, List, ListItem, Typography } from '@mui/material'
import type { IntegrationStatus } from '../types/SystemStatus'

interface IntegrationStatusListProps {
  statuses: IntegrationStatus[]
}

export function IntegrationStatusList({ statuses }: IntegrationStatusListProps) {
  return (
    <List
      disablePadding
      sx={{ bgcolor: 'background.default', borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}
    >
      {statuses.map((status, index) => (
        <ListItem
          key={status.name}
          sx={{
            gap: 2,
            py: 1.25,
            borderTop: index > 0 ? '1px solid' : 'none',
            borderColor: 'divider',
          }}
        >
          <Chip
            label={status.configured ? 'Configuré' : 'Non configuré'}
            color={status.configured ? 'success' : 'default'}
            size="small"
            variant={status.configured ? 'filled' : 'outlined'}
            sx={{ minWidth: 120 }}
          />
          <Typography sx={{ fontWeight: 600, minWidth: 90 }}>{status.name}</Typography>
          {status.detail && (
            <Typography variant="body2" color="textSecondary">
              {status.detail}
            </Typography>
          )}
        </ListItem>
      ))}
    </List>
  )
}
