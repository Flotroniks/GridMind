import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
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
    <div className={`modal ${open ? 'modal-open' : ''}`}>
      <div className="modal-box">
        <h3 className="mb-4 text-lg font-bold">{t('storage.moveStock')}</h3>

        <div className="flex flex-col gap-4">
          <label className="fieldset-label flex-col items-stretch gap-2">
            <span>{t('storage.item')}</span>
            <select
              className="select select-bordered w-full"
              value={itemId}
              onChange={(event) => setItemId(event.target.value ? Number(event.target.value) : '')}
            >
              <option value="">{t('storage.select')}</option>
              {contents.map((stock) => (
                <option key={stock.itemId} value={stock.itemId}>
                  {stock.itemName} ({t('storage.hereCount', { count: stock.quantity })})
                </option>
              ))}
            </select>
          </label>

          <label className="fieldset-label flex-col items-stretch gap-2">
            <span>{t('storage.destination')}</span>
            <select
              className="select select-bordered w-full"
              value={toLocationId}
              onChange={(event) =>
                setToLocationId(event.target.value ? Number(event.target.value) : '')
              }
            >
              <option value="">{t('storage.select')}</option>
              {locations
                .filter((location) => location.id !== fromLocationId)
                .map((location) => (
                  <option key={location.id} value={location.id}>
                    {location.path}
                  </option>
                ))}
            </select>
          </label>

          <label className="fieldset-label flex-col items-stretch gap-2">
            <span>{t('storage.quantity')}</span>
            <input
              type="number"
              min="1"
              className="input input-bordered w-full"
              value={quantity}
              onChange={(event) => setQuantity(Number(event.target.value))}
            />
          </label>
        </div>

        <div className="modal-action">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            {t('common.cancel')}
          </button>
          <button
            type="button"
            className="btn btn-primary"
            disabled={!itemId || !toLocationId || mutation.isPending}
            onClick={() => mutation.mutate()}
          >
            {t('storage.move')}
          </button>
        </div>
      </div>
    </div>
  )
}
