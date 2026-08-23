import { useEffect, useRef, useState, type ChangeEvent, type FormEvent } from 'react'
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome'
import UploadFileIcon from '@mui/icons-material/UploadFile'
import { Alert, Box, Button, Chip, CircularProgress, MenuItem, Skeleton, Stack, TextField, Typography } from '@mui/material'
import type { Category } from '@/features/categories/types/Category'
import * as imageAnalysisApi from '@/features/imageanalysis/api/imageAnalysisApi'
import { toItemInput as imageAnalysisToItemInput } from '@/features/imageanalysis/utils/toItemInput'
import { ApiError } from '@/lib/apiClient'
import type { ItemInput } from '../types/Item'

interface ItemFormProps {
  initialValue?: ItemInput
  categories: Category[]
  submitLabel: string
  onSubmit: (input: ItemInput) => Promise<void>
  onCancel: () => void
  onCreateCategory: (name: string) => Promise<Category>
  /** Shows a photo picker with a "Remplir avec IA" button that pre-fills the fields
   * below. Off by default: the catalog/photo-analysis confirm step already handles its
   * own photo, and editing an existing item doesn't support replacing its image yet. */
  enablePhotoAnalysis?: boolean
  /** Called instead of `onSubmit` when a photo was picked — uploads it as the item's
   * image. Only meaningful together with `enablePhotoAnalysis`. */
  onSubmitWithPhoto?: (input: ItemInput, photo: File) => Promise<void>
  /** Called with a keyword the last "Remplir avec IA" analysis suggested, when the user
   * wants to search the catalog providers for it instead. Only meaningful together with
   * `enablePhotoAnalysis`. */
  onSearchCatalogRequest?: (query: string) => void
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

function preferFilled(mapped: string | null | undefined, current: string | null | undefined): string | null | undefined {
  return mapped && mapped.trim() ? mapped : current
}

/** A field-shaped placeholder shown in place of a `TextField` while AI analysis is
 * filling it in — the same "shimmering block where text is about to appear" pattern
 * video sites use for loading content, applied to just the fields the analysis touches. */
function FieldSkeleton({ height = 56 }: { height?: number }) {
  return <Skeleton variant="rounded" height={height} sx={{ width: '100%', borderRadius: 1 }} />
}

export function ItemForm({
  initialValue,
  categories,
  submitLabel,
  onSubmit,
  onCancel,
  onCreateCategory,
  enablePhotoAnalysis = false,
  onSubmitWithPhoto,
  onSearchCatalogRequest,
}: ItemFormProps) {
  const [form, setForm] = useState<ItemInput>({ ...emptyForm, ...initialValue })
  const [tagsText, setTagsText] = useState((initialValue?.tags ?? []).join(', '))
  const [newCategoryName, setNewCategoryName] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const [photoFile, setPhotoFile] = useState<File | null>(null)
  const [photoPreviewUrl, setPhotoPreviewUrl] = useState<string | null>(null)
  const [analyzingPhoto, setAnalyzingPhoto] = useState(false)
  const [photoAnalysisError, setPhotoAnalysisError] = useState<string | null>(null)
  const [searchSuggestions, setSearchSuggestions] = useState<string[]>([])
  const photoInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (!photoFile) {
      setPhotoPreviewUrl(null)
      return
    }
    const url = URL.createObjectURL(photoFile)
    setPhotoPreviewUrl(url)
    return () => URL.revokeObjectURL(url)
  }, [photoFile])

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

      const input = { ...form, name: form.name.trim(), tags }
      if (photoFile && onSubmitWithPhoto) {
        await onSubmitWithPhoto(input, photoFile)
      } else {
        await onSubmit(input)
      }
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

  const handlePhotoSelected = (event: ChangeEvent<HTMLInputElement>) => {
    const selected = event.target.files?.[0] ?? null
    event.target.value = ''
    setPhotoFile(selected)
    setPhotoAnalysisError(null)
    setSearchSuggestions([])
  }

  const handleAnalyzePhoto = async () => {
    if (!photoFile) return
    setAnalyzingPhoto(true)
    setPhotoAnalysisError(null)
    try {
      const result = await imageAnalysisApi.analyzeImage(photoFile)
      const mapped = imageAnalysisToItemInput(result)
      setForm((current) => ({
        ...current,
        name: preferFilled(mapped.name, current.name) || current.name,
        manufacturer: preferFilled(mapped.manufacturer, current.manufacturer),
        reference: preferFilled(mapped.reference, current.reference),
        description: preferFilled(mapped.description, current.description),
        notes: preferFilled(mapped.notes, current.notes),
      }))
      setSearchSuggestions(result.searchQueries)
    } catch (analyzeError) {
      setPhotoAnalysisError(
        analyzeError instanceof ApiError
          ? analyzeError.message
          : 'Le serveur GridMind est injoignable. Vérifiez que le backend est démarré.',
      )
    } finally {
      setAnalyzingPhoto(false)
    }
  }

  return (
    <Stack component="form" spacing={2.5} onSubmit={handleSubmit} sx={{ pt: 1 }}>
      {enablePhotoAnalysis && (
        <Stack spacing={1.5} sx={{ p: 2, border: '1px dashed', borderColor: 'divider', borderRadius: 2 }}>
          <Typography variant="body2" color="textSecondary">
            Photo (optionnelle) — utilisée comme image de l'objet, et peut pré-remplir la fiche via
            l'IA locale.
          </Typography>
          <input
            ref={photoInputRef}
            type="file"
            accept="image/jpeg,image/png"
            hidden
            onChange={handlePhotoSelected}
          />
          <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
            <Button
              variant="outlined"
              size="small"
              startIcon={<UploadFileIcon />}
              onClick={() => photoInputRef.current?.click()}
            >
              {photoFile ? 'Changer la photo' : 'Sélectionner une photo'}
            </Button>
            {photoPreviewUrl && (
              <Box
                sx={{
                  width: 48,
                  height: 48,
                  borderRadius: 1,
                  overflow: 'hidden',
                  bgcolor: 'background.default',
                  flexShrink: 0,
                }}
              >
                <Box
                  component="img"
                  src={photoPreviewUrl}
                  alt="Photo sélectionnée"
                  sx={{ width: '100%', height: '100%', objectFit: 'contain' }}
                />
              </Box>
            )}
            {photoFile && (
              <Button
                variant="contained"
                size="small"
                startIcon={<AutoAwesomeIcon />}
                disabled={analyzingPhoto}
                onClick={() => void handleAnalyzePhoto()}
              >
                Remplir avec IA
              </Button>
            )}
          </Stack>
          {analyzingPhoto && (
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: 'text.secondary' }}>
              <CircularProgress size={14} />
              <Typography variant="body2" color="textSecondary">
                Analyse en cours… cela peut prendre quelques secondes à quelques dizaines de secondes
                sur CPU.
              </Typography>
            </Stack>
          )}
          {photoAnalysisError && <Alert severity="error">{photoAnalysisError}</Alert>}
          {searchSuggestions.length > 0 && onSearchCatalogRequest && (
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
              <Typography variant="body2" color="textSecondary">
                Pas sûr ? Rechercher dans les catalogues :
              </Typography>
              {searchSuggestions.map((query) => (
                <Chip
                  key={query}
                  label={query}
                  size="small"
                  variant="outlined"
                  clickable
                  onClick={() => onSearchCatalogRequest(query)}
                />
              ))}
            </Stack>
          )}
        </Stack>
      )}

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' }, gap: 2.5 }}>
        {analyzingPhoto ? (
          <FieldSkeleton />
        ) : (
          <TextField
            label="Nom"
            value={form.name}
            onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
            placeholder="Ex. ESP32-S3"
            autoComplete="off"
            fullWidth
          />
        )}

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

        {analyzingPhoto ? (
          <FieldSkeleton />
        ) : (
          <TextField
            label="Fabricant"
            value={form.manufacturer ?? ''}
            onChange={(event) =>
              setForm((current) => ({ ...current, manufacturer: event.target.value }))
            }
            autoComplete="off"
            fullWidth
          />
        )}

        {analyzingPhoto ? (
          <FieldSkeleton />
        ) : (
          <TextField
            label="Référence"
            value={form.reference ?? ''}
            onChange={(event) =>
              setForm((current) => ({ ...current, reference: event.target.value }))
            }
            autoComplete="off"
            fullWidth
          />
        )}

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

      {analyzingPhoto ? (
        <FieldSkeleton height={80} />
      ) : (
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
      )}

      {analyzingPhoto ? (
        <FieldSkeleton height={80} />
      ) : (
        <TextField
          label="Notes"
          value={form.notes ?? ''}
          onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
          multiline
          rows={2}
          fullWidth
        />
      )}

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
