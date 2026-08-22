import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material'
import type { Item } from '../types/Item'

interface DeleteItemDialogProps {
  item: Item | null
  onConfirm: () => void
  onCancel: () => void
}

export function DeleteItemDialog({ item, onConfirm, onCancel }: DeleteItemDialogProps) {
  return (
    <Dialog open={item !== null} onClose={onCancel}>
      <DialogTitle>Supprimer l'objet</DialogTitle>
      <DialogContent>
        <Typography>
          Confirmer la suppression de <strong>{item?.name}</strong> ? Cette action est
          irréversible.
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel}>Annuler</Button>
        <Button color="error" variant="contained" onClick={onConfirm}>
          Supprimer
        </Button>
      </DialogActions>
    </Dialog>
  )
}
