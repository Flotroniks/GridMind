import { useState, type FormEvent } from 'react'
import type { Category } from '@/features/categories/types/Category'
import type { ItemInput } from '../types/Item'

interface ItemFormProps {
  initialValue?: ItemInput
  categories: Category[]
  submitLabel: string
  onSubmit: (input: ItemInput) => Promise<void>
  onCancel: () => void
  onCreateCategory: (name: string) => Promise<Category>
}

const emptyForm: ItemInput = {
  name: '',
  quantity: 1,
  description: '',
  manufacturer: '',
  reference: '',
  categoryId: null,
  tags: [],
  notes: '',
  productUrl: '',
  datasheetUrl: '',
  minimumQuantity: 0,
}

export function ItemForm({
  initialValue,
  categories,
  submitLabel,
  onSubmit,
  onCancel,
  onCreateCategory,
}: ItemFormProps) {
  const [form, setForm] = useState<ItemInput>({ ...emptyForm, ...initialValue })
  const [tagsText, setTagsText] = useState((initialValue?.tags ?? []).join(', '))
  const [newCategoryName, setNewCategoryName] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!form.name.trim()) {
      return
    }

    setSubmitting(true)
    try {
      const tags = tagsText
        .split(',')
        .map((tag) => tag.trim())
        .filter((tag) => tag.length > 0)

      await onSubmit({ ...form, name: form.name.trim(), tags })
    } finally {
      setSubmitting(false)
    }
  }

  const handleCreateCategory = async () => {
    const name = newCategoryName.trim()
    if (!name) return
    const category = await onCreateCategory(name)
    setForm((current) => ({ ...current, categoryId: category.id }))
    setNewCategoryName('')
  }

  return (
    <form className="@container flex flex-col gap-4" onSubmit={handleSubmit}>
      <div className="grid grid-cols-1 gap-4 @md:grid-cols-2">
        <label className="fieldset-label flex-col items-stretch gap-2">
          <span>Nom</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={form.name}
            onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
            placeholder="Ex. ESP32-S3"
            autoComplete="off"
          />
        </label>

        <label className="fieldset-label flex-col items-stretch gap-2">
          <span>Quantité</span>
          <input
            type="number"
            min="0"
            className="input input-bordered w-full"
            value={form.quantity}
            onChange={(event) =>
              setForm((current) => ({ ...current, quantity: Number(event.target.value) }))
            }
          />
        </label>

        <label className="fieldset-label flex-col items-stretch gap-2">
          <span>Quantité minimale</span>
          <input
            type="number"
            min="0"
            className="input input-bordered w-full"
            value={form.minimumQuantity ?? 0}
            onChange={(event) =>
              setForm((current) => ({ ...current, minimumQuantity: Number(event.target.value) }))
            }
          />
        </label>

        <label className="fieldset-label flex-col items-stretch gap-2">
          <span>Fabricant</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={form.manufacturer ?? ''}
            onChange={(event) =>
              setForm((current) => ({ ...current, manufacturer: event.target.value }))
            }
            autoComplete="off"
          />
        </label>

        <label className="fieldset-label flex-col items-stretch gap-2">
          <span>Référence</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={form.reference ?? ''}
            onChange={(event) =>
              setForm((current) => ({ ...current, reference: event.target.value }))
            }
            autoComplete="off"
          />
        </label>

        <label className="fieldset-label flex-col items-stretch gap-2">
          <span>Catégorie</span>
          <select
            className="select select-bordered w-full"
            value={form.categoryId ?? ''}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                categoryId: event.target.value ? Number(event.target.value) : null,
              }))
            }
          >
            <option value="">Aucune</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="flex gap-2">
        <input
          type="text"
          className="input input-bordered input-sm flex-1"
          placeholder="Nouvelle catégorie…"
          value={newCategoryName}
          onChange={(event) => setNewCategoryName(event.target.value)}
          autoComplete="off"
        />
        <button type="button" className="btn btn-sm" onClick={() => void handleCreateCategory()}>
          Ajouter la catégorie
        </button>
      </div>

      <label className="fieldset-label flex-col items-stretch gap-2">
        <span>Tags (séparés par des virgules)</span>
        <input
          type="text"
          className="input input-bordered w-full"
          value={tagsText}
          onChange={(event) => setTagsText(event.target.value)}
          placeholder="wifi, 3.3v, devkit"
          autoComplete="off"
        />
      </label>

      <label className="fieldset-label flex-col items-stretch gap-2">
        <span>Description</span>
        <textarea
          className="textarea textarea-bordered w-full"
          value={form.description ?? ''}
          onChange={(event) =>
            setForm((current) => ({ ...current, description: event.target.value }))
          }
          rows={2}
        />
      </label>

      <label className="fieldset-label flex-col items-stretch gap-2">
        <span>Notes</span>
        <textarea
          className="textarea textarea-bordered w-full"
          value={form.notes ?? ''}
          onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
          rows={2}
        />
      </label>

      <div className="grid grid-cols-1 gap-4 @md:grid-cols-2">
        <label className="fieldset-label flex-col items-stretch gap-2">
          <span>URL du produit</span>
          <input
            type="url"
            className="input input-bordered w-full"
            value={form.productUrl ?? ''}
            onChange={(event) =>
              setForm((current) => ({ ...current, productUrl: event.target.value }))
            }
            autoComplete="off"
          />
        </label>

        <label className="fieldset-label flex-col items-stretch gap-2">
          <span>URL de la datasheet</span>
          <input
            type="url"
            className="input input-bordered w-full"
            value={form.datasheetUrl ?? ''}
            onChange={(event) =>
              setForm((current) => ({ ...current, datasheetUrl: event.target.value }))
            }
            autoComplete="off"
          />
        </label>
      </div>

      <div className="flex justify-end gap-2">
        <button type="button" className="btn btn-ghost" onClick={onCancel}>
          Annuler
        </button>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitLabel}
        </button>
      </div>
    </form>
  )
}
