import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  Typography,
} from '@mui/material'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import { ApiError } from '@/lib/apiClient'
import * as inventoryApi from '../api/inventoryApi'
import { ItemStockLocations } from '../components/ItemStockLocations'
import { TagBadgeList } from '../components/TagBadgeList'
import type { Item } from '../types/Item'

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

export function ItemDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const [item, setItem] = useState<Item | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    inventoryApi
      .getItem(Number(id))
      .then(setItem)
      .catch((loadError: unknown) => setError(messageOf(loadError, "Impossible de charger l'objet.")))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: 'text.secondary' }}>
        <CircularProgress size={16} />
        <Typography color="textSecondary">Chargement…</Typography>
      </Stack>
    )
  }

  if (error || !item) {
    return (
      <Alert severity="error" sx={{ mx: 'auto', width: '100%', maxWidth: 672 }}>
        {error ?? 'Objet introuvable.'}
      </Alert>
    )
  }

  return (
    <Box sx={{ mx: 'auto', width: '100%', maxWidth: 672 }}>
      <Typography
        component={Link}
        to="/"
        variant="body2"
        sx={{ mb: 2, display: 'inline-block', color: 'inherit', textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}
      >
        &larr; Retour à l'inventaire
      </Typography>

      <Card sx={{ backdropFilter: 'blur(8px)', overflow: 'hidden' }}>
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            aspectRatio: '16 / 9',
            bgcolor: 'action.hover',
            color: 'text.disabled',
          }}
        >
          <ImageOutlinedIcon sx={{ fontSize: 64 }} />
        </Box>
        <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2 }}>
            <Box>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {item.name}
              </Typography>
              {item.manufacturer && (
                <Typography variant="body2" color="textSecondary">
                  {item.manufacturer}
                  {item.reference ? ` · ${item.reference}` : ''}
                </Typography>
              )}
            </Box>
            <Typography variant="h4" color="primary" sx={{ fontWeight: 700 }}>
              {item.quantity}
            </Typography>
          </Box>

          {item.categoryName && (
            <Chip label={item.categoryName} size="small" color="primary" variant="outlined" sx={{ width: 'fit-content' }} />
          )}

          <TagBadgeList tags={item.tags} />

          {item.description && <Typography color="textSecondary">{item.description}</Typography>}

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 1.5 }}>
            <Typography variant="body2">
              <Typography component="span" variant="body2" color="textSecondary">
                Quantité minimale :{' '}
              </Typography>
              {item.minimumQuantity}
            </Typography>
            {item.productUrl && (
              <Typography variant="body2">
                <Typography component="a" href={item.productUrl} target="_blank" rel="noreferrer" color="primary">
                  Page produit
                </Typography>
              </Typography>
            )}
            {item.datasheetUrl && (
              <Typography variant="body2">
                <Typography component="a" href={item.datasheetUrl} target="_blank" rel="noreferrer" color="primary">
                  Datasheet
                </Typography>
              </Typography>
            )}
          </Box>

          {item.notes && (
            <Box>
              <Typography variant="body2" color="textSecondary">
                Notes
              </Typography>
              <Typography>{item.notes}</Typography>
            </Box>
          )}

          <Box>
            <Typography variant="body2" color="textSecondary" sx={{ mb: 1 }}>
              Emplacements de stockage
            </Typography>
            <ItemStockLocations itemId={item.id} />
          </Box>
        </CardContent>
      </Card>
    </Box>
  )
}
