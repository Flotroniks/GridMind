import { useState, type FormEvent } from 'react'
import { Box, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
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
  quantityHs: 0,
  quantityInUse: 0,
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
    <Stack component="form" spacing={2.5} onSubmit={handleSubmit} sx={{ pt: 1 }}>
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' }, gap: 2.5 }}>
        <TextField
          label="Nom"
          value={form.name}
          onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
          placeholder="Ex. ESP32-S3"
          autoComplete="off"
          fullWidth
        />

        <TextField
          label="Quantité"
          type="number"
          slotProps={{ htmlInput: { min: 0 } }}
          value={form.quantity}
          onChange={(event) =>
            setForm((current) => ({ ...current, quantity: Number(event.target.value) }))
          }
          fullWidth
        />

        <TextField
          label="Quantité minimale"
          type="number"
          slotProps={{ htmlInput: { min: 0 } }}
          value={form.minimumQuantity ?? 0}
          onChange={(event) =>
            setForm((current) => ({ ...current, minimumQuantity: Number(event.target.value) }))
          }
          fullWidth
        />

        <TextField
          label="Quantité HS"
          type="number"
          slotProps={{ htmlInput: { min: 0 } }}
          value={form.quantityHs ?? 0}
          onChange={(event) =>
            setForm((current) => ({ ...current, quantityHs: Number(event.target.value) }))
          }
          fullWidth
        />

        <TextField
          label="Quantité en utilisation"
          type="number"
          slotProps={{ htmlInput: { min: 0 } }}
          value={form.quantityInUse ?? 0}
          onChange={(event) =>
            setForm((current) => ({ ...current, quantityInUse: Number(event.target.value) }))
          }
          fullWidth
        />

        <TextField
          label="Fabricant"
          value={form.manufacturer ?? ''}
          onChange={(event) =>
            setForm((current) => ({ ...current, manufacturer: event.target.value }))
          }
          autoComplete="off"
          fullWidth
        />

        <TextField
          label="Référence"
          value={form.reference ?? ''}
          onChange={(event) =>
            setForm((current) => ({ ...current, reference: event.target.value }))
          }
          autoComplete="off"
          fullWidth
        />

        <TextField
          select
          label="Catégorie"
          value={form.categoryId ?? ''}
          onChange={(event) =>
            setForm((current) => ({
              ...current,
              categoryId: event.target.value ? Number(event.target.value) : null,
            }))
          }
          fullWidth
          slotProps={{ select: { displayEmpty: true }, inputLabel: { shrink: true } }}
        >
          <MenuItem value="">Aucune</MenuItem>
          {categories.map((category) => (
            <MenuItem key={category.id} value={category.id}>
              {category.name}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <Typography variant="body2" color="textSecondary">
        {Math.max(0, form.quantity - (form.quantityHs ?? 0) - (form.quantityInUse ?? 0))} prêt(s) à
        utiliser sur {form.quantity}
      </Typography>

      <Stack direction="row" spacing={1}>
        <TextField
          size="small"
          placeholder="Nouvelle catégorie…"
          value={newCategoryName}
          onChange={(event) => setNewCategoryName(event.target.value)}
          autoComplete="off"
          fullWidth
        />
        <Button
          variant="outlined"
          onClick={() => void handleCreateCategory()}
          sx={{ whiteSpace: 'nowrap', flexShrink: 0 }}
        >
          Ajouter la catégorie
        </Button>
      </Stack>

      <TextField
        label="Tags (séparés par des virgules)"
        value={tagsText}
        onChange={(event) => setTagsText(event.target.value)}
        placeholder="wifi, 3.3v, devkit"
        autoComplete="off"
        fullWidth
      />

      <TextField
        label="Description"
        value={form.description ?? ''}
        onChange={(event) =>
          setForm((current) => ({ ...current, description: event.target.value }))
        }
        multiline
        rows={2}
        fullWidth
      />

      <TextField
        label="Notes"
        value={form.notes ?? ''}
        onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
        multiline
        rows={2}
        fullWidth
      />

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' }, gap: 2.5 }}>
        <TextField
          label="URL du produit"
          type="url"
          value={form.productUrl ?? ''}
          onChange={(event) =>
            setForm((current) => ({ ...current, productUrl: event.target.value }))
          }
          autoComplete="off"
          fullWidth
        />

        <TextField
          label="URL de la datasheet"
          type="url"
          value={form.datasheetUrl ?? ''}
          onChange={(event) =>
            setForm((current) => ({ ...current, datasheetUrl: event.target.value }))
          }
          autoComplete="off"
          fullWidth
        />
      </Box>

      <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
        <Button onClick={onCancel}>Annuler</Button>
        <Button type="submit" variant="contained" disabled={submitting}>
          {submitLabel}
        </Button>
      </Stack>
    </Stack>
  )
}
