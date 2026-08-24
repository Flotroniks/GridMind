import { Chip, Stack, Typography } from '@mui/material'
import type { IntegrationStatus } from '../types/SystemStatus'

interface IntegrationStatusListProps {
  statuses: IntegrationStatus[]
}

export function IntegrationStatusList({ statuses }: IntegrationStatusListProps) {
  return (
    <Stack spacing={1.5}>
      {statuses.map((status) => (
        <Stack
          key={status.name}
          direction="row"
          spacing={2}
          sx={{ alignItems: 'center', py: 1, px: 1.5, borderRadius: 1.5, bgcolor: 'background.default' }}
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
        </Stack>
      ))}
    </Stack>
  )
}
