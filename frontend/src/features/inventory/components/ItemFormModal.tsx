import { Dialog, DialogContent, DialogTitle } from '@mui/material'
import type { Category } from '@/features/categories/types/Category'
import type { CatalogResult } from '@/features/catalog/types/CatalogResult'
import type { ItemInput } from '../types/Item'
import { ItemForm } from './ItemForm'

interface ItemFormModalProps {
  open: boolean
  title: string
  initialValue?: ItemInput
  categories: Category[]
  submitLabel: string
  onSubmit: (input: ItemInput) => Promise<void>
  onClose: () => void
  onCreateCategory: (name: string) => Promise<Category>
  enablePhotoAnalysis?: boolean
  onSubmitWithPhoto?: (input: ItemInput, photo: File) => Promise<void>
  onSearchCatalogRequest?: (query: string) => void
  onCatalogResultSelected?: (result: CatalogResult) => void
}

export function ItemFormModal({
  open,
  title,
  initialValue,
  categories,
  submitLabel,
  onSubmit,
  onClose,
  onCreateCategory,
  enablePhotoAnalysis,
  onSubmitWithPhoto,
  onSearchCatalogRequest,
  onCatalogResultSelected,
}: ItemFormModalProps) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        {open && (
          <ItemForm
            initialValue={initialValue}
            categories={categories}
            submitLabel={submitLabel}
            onCancel={onClose}
            onCreateCategory={onCreateCategory}
            enablePhotoAnalysis={enablePhotoAnalysis}
            onSubmit={async (input) => {
              await onSubmit(input)
              onClose()
            }}
            onSubmitWithPhoto={
              onSubmitWithPhoto
                ? async (input, photo) => {
                    await onSubmitWithPhoto(input, photo)
                    onClose()
                  }
                : undefined
            }
            onSearchCatalogRequest={
              onSearchCatalogRequest
                ? (query) => {
                    onClose()
                    onSearchCatalogRequest(query)
                  }
                : undefined
            }
            onCatalogResultSelected={
              onCatalogResultSelected
                ? (result) => {
                    onClose()
                    onCatalogResultSelected(result)
                  }
                : undefined
            }
          />
        )}
      </DialogContent>
    </Dialog>
  )
}
