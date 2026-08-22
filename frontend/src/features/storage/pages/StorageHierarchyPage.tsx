import { useState } from 'react'
import { Box, Button, Card, CardContent, Stack, Typography } from '@mui/material'
import { CreateLocationForm } from '../components/CreateLocationForm'
import { LocationContents } from '../components/LocationContents'
import { LocationTree } from '../components/LocationTree'
import { MoveStockDialog } from '../components/MoveStockDialog'

export function StorageHierarchyPage() {
  const [breadcrumb, setBreadcrumb] = useState<{ id: number; name: string }[]>([])
  const [movingStock, setMovingStock] = useState(false)

  const currentId = breadcrumb.length > 0 ? breadcrumb[breadcrumb.length - 1].id : null

  const handleSelect = (id: number, name: string) => {
    setBreadcrumb((current) => [...current, { id, name }])
  }

  const handleNavigateBreadcrumb = (index: number) => {
    setBreadcrumb((current) => (index < 0 ? [] : current.slice(0, index + 1)))
  }

  return (
    <Box sx={{ mx: 'auto', width: { xs: '100%', sm: '75vw' }, maxWidth: 1600 }}>
      <Card sx={{ backdropFilter: 'blur(8px)' }}>
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ mb: 2 }}>
            <Typography variant="overline" color="primary" sx={{ fontWeight: 600, letterSpacing: 2 }}>
              GridMind
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>
              Stockage
            </Typography>
          </Box>

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 3 }}>
            <Box>
              <LocationTree
                parentId={currentId}
                breadcrumb={breadcrumb}
                onSelect={handleSelect}
                onNavigateBreadcrumb={handleNavigateBreadcrumb}
              />
              <Box sx={{ mt: 2 }}>
                <CreateLocationForm parentId={currentId} />
              </Box>
            </Box>

            <Box>
              <Stack
                direction="row"
                sx={{ mb: 1, alignItems: 'center', justifyContent: 'space-between' }}
              >
                <Typography sx={{ fontWeight: 600 }}>Contenu</Typography>
                {currentId != null && (
                  <Button size="small" onClick={() => setMovingStock(true)}>
                    Déplacer du stock
                  </Button>
                )}
              </Stack>
              <LocationContents locationId={currentId} />
            </Box>
          </Box>
        </CardContent>
      </Card>

      <MoveStockDialog
        open={movingStock}
        fromLocationId={currentId}
        onClose={() => setMovingStock(false)}
      />
    </Box>
  )
}
