import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material'
import { useToast } from '@/components/common/useToast'
import { ApiError } from '@/lib/apiClient'
import * as storageApi from '../api/storageApi'

interface MoveStockDialogProps {
  open: boolean
  fromLocationId: number | null
  onClose: () => void
}

export function MoveStockDialog({ open, fromLocationId, onClose }: MoveStockDialogProps) {
  const { t } = useTranslation()
  const { showToast } = useToast()
  const queryClient = useQueryClient()
  const [itemId, setItemId] = useState<number | ''>('')
  const [toLocationId, setToLocationId] = useState<number | ''>('')
  const [quantity, setQuantity] = useState(1)

  const { data: contents = [] } = useQuery({
    queryKey: ['storage', 'contents', fromLocationId],
    queryFn: () => storageApi.getLocationContents(fromLocationId!),
    enabled: open && fromLocationId != null,
  })

  const { data: locations = [] } = useQuery({
    queryKey: ['storage', 'allLocations'],
    queryFn: storageApi.listAllLocationsFlat,
    enabled: open,
  })

  const mutation = useMutation({
    mutationFn: () => storageApi.moveStock(Number(itemId), fromLocationId!, Number(toLocationId), quantity),
    onSuccess: () => {
      showToast(t('storage.moveSuccess'), 'success')
      void queryClient.invalidateQueries({ queryKey: ['storage', 'contents', fromLocationId] })
      void queryClient.invalidateQueries({ queryKey: ['storage', 'contents', Number(toLocationId)] })
      void queryClient.invalidateQueries({ queryKey: ['storage', 'itemStock'] })
      setItemId('')
      setToLocationId('')
      setQuantity(1)
      onClose()
    },
    onError: (error: unknown) => {
      const message =
        error instanceof ApiError || error instanceof Error ? error.message : t('storage.moveError')
      showToast(message, 'error')
    },
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{t('storage.moveStock')}</DialogTitle>
      <DialogContent>
        <Stack spacing={2.5} sx={{ pt: 1 }}>
          <TextField
            select
            label={t('storage.item')}
            value={itemId}
            onChange={(event) => setItemId(event.target.value ? Number(event.target.value) : '')}
            fullWidth
          >
            <MenuItem value="">{t('storage.select')}</MenuItem>
            {contents.map((stock) => (
              <MenuItem key={stock.itemId} value={stock.itemId}>
                {stock.itemName} ({t('storage.hereCount', { count: stock.quantity })})
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

          <TextField
            label={t('storage.quantity')}
            type="number"
            slotProps={{ htmlInput: { min: 1 } }}
            value={quantity}
            onChange={(event) => setQuantity(Number(event.target.value))}
            fullWidth
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{t('common.cancel')}</Button>
        <Button
          variant="contained"
          disabled={!itemId || !toLocationId || mutation.isPending}
          onClick={() => mutation.mutate()}
        >
          {t('storage.move')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
