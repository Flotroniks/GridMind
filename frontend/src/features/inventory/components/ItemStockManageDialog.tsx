import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useToast } from '@/components/common/useToast'
import * as storageApi from '@/features/storage/api/storageApi'
import { ApiError } from '@/lib/apiClient'

interface ItemStockManageDialogProps {
  open: boolean
  itemId: number
  itemName: string
  onClose: () => void
}

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

/**
 * Lets a user manage where an item is physically stored directly from its own detail
 * page — allocate it to a location, and move it between locations — instead of the only
 * path being the separate Storage page's location-scoped "Déplacer du stock" dialog,
 * which requires already knowing (and navigating to) the item's current location first.
 */
export function ItemStockManageDialog({ open, itemId, itemName, onClose }: ItemStockManageDialogProps) {
  const { t } = useTranslation()
  const { showToast } = useToast()
  const queryClient = useQueryClient()

  const [allocateLocationId, setAllocateLocationId] = useState<number | ''>('')
  const [allocateQuantity, setAllocateQuantity] = useState(1)

  const [fromLocationId, setFromLocationId] = useState<number | ''>('')
  const [toLocationId, setToLocationId] = useState<number | ''>('')
  const [moveQuantity, setMoveQuantity] = useState(1)

  const { data: stock = [] } = useQuery({
    queryKey: ['storage', 'itemStock', itemId],
    queryFn: () => storageApi.getItemStock(itemId),
    enabled: open,
  })

  const { data: locations = [] } = useQuery({
    queryKey: ['storage', 'allLocations'],
    queryFn: storageApi.listAllLocationsFlat,
    enabled: open,
  })

  const invalidateStock = () => {
    void queryClient.invalidateQueries({ queryKey: ['storage', 'itemStock', itemId] })
    void queryClient.invalidateQueries({ queryKey: ['storage', 'contents'] })
  }

  const allocateMutation = useMutation({
    mutationFn: () => storageApi.allocateStock(itemId, Number(allocateLocationId), allocateQuantity),
    onSuccess: () => {
      showToast(t('storage.allocateSuccess'), 'success')
      invalidateStock()
      setAllocateLocationId('')
      setAllocateQuantity(1)
    },
    onError: (error: unknown) => showToast(messageOf(error, t('storage.allocateError')), 'error'),
  })

  const moveMutation = useMutation({
    mutationFn: () =>
      storageApi.moveStock(itemId, Number(fromLocationId), Number(toLocationId), moveQuantity),
    onSuccess: () => {
      showToast(t('storage.moveSuccess'), 'success')
      invalidateStock()
      setFromLocationId('')
      setToLocationId('')
      setMoveQuantity(1)
    },
    onError: (error: unknown) => showToast(messageOf(error, t('storage.moveError')), 'error'),
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{t('storage.manageStockFor', { name: itemName })}</DialogTitle>
      <DialogContent>
        <Stack spacing={2.5} sx={{ pt: 1 }}>
          {stock.length === 0 ? (
            <Typography variant="body2" color="textSecondary">
              {t('storage.notStoredAnywhere')}
            </Typography>
          ) : (
            <Stack spacing={1}>
              {stock.map((entry) => (
                <Stack
                  key={entry.storageLocationId}
                  direction="row"
                  sx={{
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
                </Stack>
              ))}
            </Stack>
          )}

          <Divider />

          <Typography variant="subtitle2" color="textSecondary">
            {t('storage.addLocation')}
          </Typography>
          <Stack direction="row" spacing={1.5}>
            <TextField
              select
              label={t('storage.location')}
              value={allocateLocationId}
              onChange={(event) =>
                setAllocateLocationId(event.target.value ? Number(event.target.value) : '')
              }
              fullWidth
              slotProps={{ select: { displayEmpty: true }, inputLabel: { shrink: true } }}
            >
              <MenuItem value="">{t('storage.select')}</MenuItem>
              {locations.map((location) => (
                <MenuItem key={location.id} value={location.id}>
                  {location.path}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label={t('storage.quantity')}
              type="number"
              slotProps={{ htmlInput: { min: 0 } }}
              value={allocateQuantity}
              onChange={(event) => setAllocateQuantity(Number(event.target.value))}
              sx={{ minWidth: 120 }}
            />
            <Button
              variant="outlined"
              disabled={!allocateLocationId || allocateMutation.isPending}
              onClick={() => allocateMutation.mutate()}
              sx={{ whiteSpace: 'nowrap' }}
            >
              {t('storage.allocate')}
            </Button>
          </Stack>

          <Divider />

          <Typography variant="subtitle2" color="textSecondary">
            {t('storage.moveStock')}
          </Typography>
          {stock.length === 0 ? (
            <Typography variant="body2" color="textSecondary">
              {t('storage.moveNeedsSource')}
            </Typography>
          ) : (
            <Stack spacing={1.5}>
              <TextField
                select
                label={t('storage.from')}
                value={fromLocationId}
                onChange={(event) =>
                  setFromLocationId(event.target.value ? Number(event.target.value) : '')
                }
                fullWidth
                slotProps={{ select: { displayEmpty: true }, inputLabel: { shrink: true } }}
              >
                <MenuItem value="">{t('storage.select')}</MenuItem>
                {stock.map((entry) => (
                  <MenuItem key={entry.storageLocationId} value={entry.storageLocationId}>
                    {entry.storageLocationName} ({t('storage.hereCount', { count: entry.quantity })})
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                select
                label={t('storage.destination')}
                value={toLocationId}
                onChange={(event) =>
                  setToLocationId(event.target.value ? Number(event.target.value) : '')
                }
                fullWidth
                slotProps={{ select: { displayEmpty: true }, inputLabel: { shrink: true } }}
              >
                <MenuItem value="">{t('storage.select')}</MenuItem>
                {locations
                  .filter((location) => location.id !== fromLocationId)
                  .map((location) => (
                    <MenuItem key={location.id} value={location.id}>
                      {location.path}
                    </MenuItem>
                  ))}
              </TextField>

              <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                <TextField
                  label={t('storage.quantity')}
                  type="number"
                  slotProps={{ htmlInput: { min: 1 } }}
                  value={moveQuantity}
                  onChange={(event) => setMoveQuantity(Number(event.target.value))}
                  sx={{ minWidth: 120 }}
                />
                <Button
                  variant="outlined"
                  disabled={!fromLocationId || !toLocationId || moveMutation.isPending}
                  onClick={() => moveMutation.mutate()}
                >
                  {t('storage.move')}
                </Button>
              </Stack>
            </Stack>
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{t('common.cancel')}</Button>
      </DialogActions>
    </Dialog>
  )
}
