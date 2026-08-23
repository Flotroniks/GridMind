import { Dialog, DialogContent, DialogTitle } from '@mui/material'
import type { CatalogResult } from '../types/CatalogResult'
import { CatalogMatchPicker } from './CatalogMatchPicker'

interface CatalogMatchPickerDialogProps {
  open: boolean
  queries: string[]
  onSelect: (result: CatalogResult) => void
  onSkip: () => void
}

/** Thin `Dialog` wrapper around `CatalogMatchPicker` for contexts with no host dialog of
 * their own to render it into — the manual `ItemForm`'s inline "Remplir avec IA", unlike
 * the guided flow which already renders inside `ProductSearchModal`'s dialog. */
export function CatalogMatchPickerDialog({ open, queries, onSelect, onSkip }: CatalogMatchPickerDialogProps) {
  return (
    <Dialog open={open} onClose={onSkip} maxWidth="md" fullWidth>
      <DialogTitle>Correspondances trouvées</DialogTitle>
      <DialogContent>{open && <CatalogMatchPicker queries={queries} onSelect={onSelect} onSkip={onSkip} />}</DialogContent>
    </Dialog>
  )
}
