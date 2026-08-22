import { Dialog, DialogContent, DialogTitle } from '@mui/material'
import type { Category } from '@/features/categories/types/Category'
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
            onSubmit={async (input) => {
              await onSubmit(input)
              onClose()
            }}
          />
        )}
      </DialogContent>
    </Dialog>
  )
}
