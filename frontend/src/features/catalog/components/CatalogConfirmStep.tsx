import { useState } from 'react'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import { Box, Button, Chip, Divider, Stack, Typography } from '@mui/material'
import type { Category } from '@/features/categories/types/Category'
import { ItemForm } from '@/features/inventory/components/ItemForm'
import type { ItemInput } from '@/features/inventory/types/Item'
import type { CatalogImage, CatalogResult } from '../types/CatalogResult'

export interface CatalogImageSource {
  url: string
  provider: string
}

interface CatalogConfirmStepProps {
  result: CatalogResult
  categories: Category[]
  onCreateCategory: (name: string) => Promise<Category>
  onCancel: () => void
  onSubmit: (input: ItemInput, source: CatalogImageSource | null) => Promise<void>
}

function findMatchingCategoryId(categoryName: string | null, categories: Category[]): number | null {
  if (!categoryName) return null
  const normalized = categoryName.trim().toLowerCase()
  return categories.find((category) => category.name.toLowerCase() === normalized)?.id ?? null
}

function toItemInput(result: CatalogResult, categoryId: number | null): ItemInput {
  return {
    name: result.name,
    quantity: 1,
    manufacturer: result.manufacturer,
    reference: result.mpn,
    description: result.description,
    datasheetUrl: result.datasheetUrl,
    categoryId,
  }
}

export function CatalogConfirmStep({
  result,
  categories,
  onCreateCategory,
  onCancel,
  onSubmit,
}: CatalogConfirmStepProps) {
  const [selectedImage, setSelectedImage] = useState<CatalogImage | null>(result.images[0] ?? null)
  const [matchedCategoryId, setMatchedCategoryId] = useState<number | null>(() =>
    findMatchingCategoryId(result.category, categories),
  )
  const [formValue, setFormValue] = useState<ItemInput>(() => toItemInput(result, matchedCategoryId))
  const [step, setStep] = useState<'form' | 'review'>('form')
  const [confirming, setConfirming] = useState(false)

  const handleCreateSuggestedCategory = async () => {
    if (!result.category) return
    const category = await onCreateCategory(result.category)
    setMatchedCategoryId(category.id)
    setFormValue((current) => ({ ...current, categoryId: category.id }))
  }

  const handleConfirm = async () => {
    setConfirming(true)
    try {
      await onSubmit(
        formValue,
        selectedImage ? { url: selectedImage.url, provider: selectedImage.provider } : null,
      )
    } finally {
      setConfirming(false)
    }
  }

  if (step === 'review') {
    const categoryName = categories.find((category) => category.id === formValue.categoryId)?.name ?? null

    return (
      <Stack spacing={2.5}>
        <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
          Vérifiez avant d'ajouter
        </Typography>

        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
          <Box
            sx={{
              width: 72,
              height: 72,
              borderRadius: 1.5,
              overflow: 'hidden',
              bgcolor: 'background.default',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'text.disabled',
              flexShrink: 0,
            }}
          >
            {selectedImage ? (
              <Box
                component="img"
                src={selectedImage.url}
                alt={formValue.name}
                sx={{ width: '100%', height: '100%', objectFit: 'contain' }}
              />
            ) : (
              <ImageOutlinedIcon />
            )}
          </Box>
          <Stack spacing={0.5}>
            <Typography variant="body1" sx={{ fontWeight: 600 }}>
              {formValue.name}
            </Typography>
            {(formValue.manufacturer || formValue.reference) && (
              <Typography variant="body2" color="textSecondary">
                {[formValue.manufacturer, formValue.reference]
                  .filter((value): value is string => Boolean(value))
                  .join(' · ')}
              </Typography>
            )}
          </Stack>
        </Stack>

        <Divider />

        <Stack spacing={1}>
          <Typography variant="body2">
            Quantité : <strong>{formValue.quantity}</strong>
          </Typography>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <Typography variant="body2">Catégorie :</Typography>
            {categoryName ? (
              <Chip component="span" label={categoryName} size="small" />
            ) : (
              <Typography variant="body2" color="textSecondary">
                Aucune
              </Typography>
            )}
          </Stack>
        </Stack>

        <Typography variant="body2" color="textSecondary">
          Tout est bon ?
        </Typography>

        <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
          <Button onClick={() => setStep('form')} disabled={confirming}>
            Modifier
          </Button>
          <Button variant="contained" disabled={confirming} onClick={() => void handleConfirm()}>
            Confirmer et ajouter
          </Button>
        </Stack>
      </Stack>
    )
  }

  return (
    <Stack spacing={2.5}>
      {result.category && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
          <Typography variant="body2" color="textSecondary">
            {matchedCategoryId
              ? 'Catégorie du catalogue (déjà sélectionnée ci-dessous) :'
              : 'Catégorie suggérée par le catalogue :'}
          </Typography>
          <Chip component="span" label={result.category} size="small" variant="outlined" />
          {!matchedCategoryId && (
            <Button size="small" onClick={() => void handleCreateSuggestedCategory()}>
              Créer et utiliser
            </Button>
          )}
        </Stack>
      )}

      {result.images.length > 0 && (
        <Box>
          <Typography variant="body2" color="textSecondary" sx={{ mb: 1 }}>
            Image
          </Typography>
          <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
            {result.images.map((image) => {
              const isSelected = selectedImage?.url === image.url
              return (
                <Box
                  key={image.url}
                  component="button"
                  type="button"
                  onClick={() => setSelectedImage(image)}
                  sx={{
                    p: 0,
                    width: 84,
                    height: 84,
                    borderRadius: 1.5,
                    overflow: 'hidden',
                    cursor: 'pointer',
                    border: '2px solid',
                    borderColor: isSelected ? 'primary.main' : 'divider',
                    bgcolor: 'background.default',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Box
                    component="img"
                    src={image.url}
                    alt={image.provider}
                    sx={{ width: '100%', height: '100%', objectFit: 'contain' }}
                  />
                </Box>
              )
            })}
            <Box
              component="button"
              type="button"
              onClick={() => setSelectedImage(null)}
              sx={{
                p: 0,
                width: 84,
                height: 84,
                borderRadius: 1.5,
                cursor: 'pointer',
                border: '2px solid',
                borderColor: selectedImage === null ? 'primary.main' : 'divider',
                bgcolor: 'background.default',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'text.disabled',
              }}
              aria-label="Aucune image"
            >
              <ImageOutlinedIcon />
            </Box>
          </Stack>
        </Box>
      )}

      <ItemForm
        // Remounts with the freshly created category pre-selected — the "Créer et
        // utiliser" action above only ever fires before the user has edited much, so
        // resetting other in-progress edits here is an acceptable trade-off for keeping
        // ItemForm (which owns its state internally, uncontrolled) simple.
        key={matchedCategoryId ?? 'none'}
        initialValue={formValue}
        categories={categories}
        submitLabel="Continuer"
        onCancel={onCancel}
        onCreateCategory={onCreateCategory}
        onSubmit={async (input) => {
          setFormValue(input)
          setStep('review')
        }}
      />
    </Stack>
  )
}
