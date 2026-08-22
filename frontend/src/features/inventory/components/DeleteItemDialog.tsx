import type { Item } from '../types/Item'

interface DeleteItemDialogProps {
  item: Item | null
  onConfirm: () => void
  onCancel: () => void
}

export function DeleteItemDialog({ item, onConfirm, onCancel }: DeleteItemDialogProps) {
  return (
    <div className={`modal ${item ? 'modal-open' : ''}`}>
      <div className="modal-box">
        <h3 className="text-lg font-bold">Supprimer l'objet</h3>
        <p className="py-4">
          Confirmer la suppression de <strong>{item?.name}</strong> ? Cette action est
          irréversible.
        </p>
        <div className="modal-action">
          <button type="button" className="btn btn-ghost" onClick={onCancel}>
            Annuler
          </button>
          <button type="button" className="btn btn-error" onClick={onConfirm}>
            Supprimer
          </button>
        </div>
      </div>
    </div>
  )
}
