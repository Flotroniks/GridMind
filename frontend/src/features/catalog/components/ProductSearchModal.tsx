import { useState } from 'react'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { Dialog, DialogContent, DialogTitle, IconButton, Stack } from '@mui/material'
import type { Category } from '@/features/categories/types/Category'
import type { ItemInput } from '@/features/inventory/types/Item'
import { CatalogConfirmStep, type CatalogImageSource } from './CatalogConfirmStep'
import { CatalogSearchStep } from './CatalogSearchStep'
import type { CatalogResult } from '../types/CatalogResult'

interface ProductSearchModalProps {
  open: boolean
  categories: Category[]
  onClose: () => void
  onCreateCategory: (name: string) => Promise<Category>
  onSubmit: (input: ItemInput, source: CatalogImageSource | null) => Promise<void>
  onManualCreateRequest: (query: string) => void
}

export function ProductSearchModal({
  open,
  categories,
  onClose,
  onCreateCategory,
  onSubmit,
  onManualCreateRequest,
}: ProductSearchModalProps) {
  const [selectedResult, setSelectedResult] = useState<CatalogResult | null>(null)

  const handleClose = () => {
    setSelectedResult(null)
    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          {selectedResult && (
            <IconButton
              size="small"
              onClick={() => setSelectedResult(null)}
              aria-label="Retour à la recherche"
              sx={{ ml: -1 }}
            >
              <ArrowBackIcon fontSize="small" />
            </IconButton>
          )}
          <span>{selectedResult ? "Confirmer l'objet" : 'Rechercher un produit'}</span>
        </Stack>
      </DialogTitle>
      <DialogContent>
        {open &&
          (selectedResult ? (
            <CatalogConfirmStep
              result={selectedResult}
              categories={categories}
              onCreateCategory={onCreateCategory}
              onCancel={handleClose}
              onSubmit={async (input, source) => {
                await onSubmit(input, source)
                handleClose()
              }}
            />
          ) : (
            <CatalogSearchStep
              onSelect={setSelectedResult}
              onManualCreateRequest={(query) => {
                handleClose()
                onManualCreateRequest(query)
              }}
            />
          ))}
      </DialogContent>
    </Dialog>
  )
}
