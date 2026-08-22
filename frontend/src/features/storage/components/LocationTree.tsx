import { useQuery } from '@tanstack/react-query'
import {
  Box,
  Breadcrumbs,
  CircularProgress,
  Link as MuiLink,
  List,
  ListItemButton,
  ListItemText,
  Typography,
} from '@mui/material'
import * as storageApi from '../api/storageApi'

interface LocationTreeProps {
  parentId: number | null
  breadcrumb: { id: number; name: string }[]
  onSelect: (id: number, name: string) => void
  onNavigateBreadcrumb: (index: number) => void
}

export function LocationTree({
  parentId,
  breadcrumb,
  onSelect,
  onNavigateBreadcrumb,
}: LocationTreeProps) {
  const { data: children = [], isLoading } = useQuery({
    queryKey: ['storage', 'children', parentId],
    queryFn: () => storageApi.listLocations(parentId),
  })

  return (
    <Box>
      <Breadcrumbs sx={{ mb: 1.5 }}>
        <MuiLink component="button" type="button" underline="hover" onClick={() => onNavigateBreadcrumb(-1)}>
          Racine
        </MuiLink>
        {breadcrumb.map((crumb, index) => (
          <MuiLink
            key={crumb.id}
            component="button"
            type="button"
            underline="hover"
            onClick={() => onNavigateBreadcrumb(index)}
          >
            {crumb.name}
          </MuiLink>
        ))}
      </Breadcrumbs>

      {isLoading ? (
        <CircularProgress size={16} />
      ) : children.length === 0 ? (
        <Typography variant="body2" color="textSecondary">
          Aucun emplacement enfant.
        </Typography>
      ) : (
        <List disablePadding sx={{ bgcolor: 'background.default', borderRadius: 2 }}>
          {children.map((child) => (
            <ListItemButton key={child.id} onClick={() => onSelect(child.id, child.name)}>
              <ListItemText primary={child.name} />
              {child.hasChildren && (
                <Typography color="textDisabled">&rarr;</Typography>
              )}
            </ListItemButton>
          ))}
        </List>
      )}
    </Box>
  )
}
