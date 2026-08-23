import { useEffect, useState } from 'react'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { Dialog, DialogContent, DialogTitle, IconButton, Stack } from '@mui/material'
import type { Category } from '@/features/categories/types/Category'
import { AnalyzeImageStep } from '@/features/imageanalysis/components/AnalyzeImageStep'
import type { ImageAnalysisResult } from '@/features/imageanalysis/types/ImageAnalysis'
import { toItemInput as imageAnalysisToItemInput } from '@/features/imageanalysis/utils/toItemInput'
import {
  PrefilledItemConfirmStep,
  type CatalogImageSource,
} from '@/features/inventory/components/PrefilledItemConfirmStep'
import type { ItemInput } from '@/features/inventory/types/Item'
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

type Step =
  | { kind: 'search' }
  | { kind: 'analyze-photo' }
  | { kind: 'confirm-catalog'; result: CatalogResult }
  | { kind: 'confirm-analysis'; result: ImageAnalysisResult }

const TITLES: Record<Step['kind'], string> = {
  search: 'Rechercher un produit',
  'analyze-photo': 'Analyser une photo',
  'confirm-catalog': "Confirmer l'objet",
  'confirm-analysis': "Confirmer l'objet",
}

export function ProductSearchModal({
  open,
  categories,
  onClose,
  onCreateCategory,
  onSubmit,
  onManualCreateRequest,
}: ProductSearchModalProps) {
  const [step, setStep] = useState<Step>({ kind: 'search' })
  const [photoFile, setPhotoFile] = useState<File | null>(null)
  const [photoPreviewUrl, setPhotoPreviewUrl] = useState<string | null>(null)

  // Owned here, not in AnalyzeImageStep: the preview URL must stay valid across the
  // transition into the confirm step, which happens after AnalyzeImageStep unmounts.
  useEffect(() => {
    if (!photoFile) {
      setPhotoPreviewUrl(null)
      return
    }
    const url = URL.createObjectURL(photoFile)
    setPhotoPreviewUrl(url)
    return () => URL.revokeObjectURL(url)
  }, [photoFile])

  const handleClose = () => {
    setStep({ kind: 'search' })
    setPhotoFile(null)
    onClose()
  }

  const handleSubmit = async (input: ItemInput, source: CatalogImageSource | null) => {
    await onSubmit(input, source)
    handleClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          {step.kind !== 'search' && (
            <IconButton
              size="small"
              onClick={() => setStep({ kind: 'search' })}
              aria-label="Retour à la recherche"
              sx={{ ml: -1 }}
            >
              <ArrowBackIcon fontSize="small" />
            </IconButton>
          )}
          <span>{TITLES[step.kind]}</span>
        </Stack>
      </DialogTitle>
      <DialogContent>
        {open && step.kind === 'search' && (
          <CatalogSearchStep
            onSelect={(result) => setStep({ kind: 'confirm-catalog', result })}
            onManualCreateRequest={(query) => {
              handleClose()
              onManualCreateRequest(query)
            }}
            onAnalyzePhotoRequest={() => setStep({ kind: 'analyze-photo' })}
          />
        )}

        {open && step.kind === 'analyze-photo' && (
          <AnalyzeImageStep
            file={photoFile}
            previewUrl={photoPreviewUrl}
            onFileSelected={setPhotoFile}
            onAnalyzed={(result) => setStep({ kind: 'confirm-analysis', result })}
          />
        )}

        {open && step.kind === 'confirm-catalog' && (
          <PrefilledItemConfirmStep
            initialInput={{
              name: step.result.name,
              quantity: 1,
              manufacturer: step.result.manufacturer,
              reference: step.result.mpn,
              description: step.result.description,
              datasheetUrl: step.result.datasheetUrl,
            }}
            suggestedCategoryName={step.result.category}
            images={step.result.images}
            categories={categories}
            onCreateCategory={onCreateCategory}
            onCancel={handleClose}
            onSubmit={handleSubmit}
          />
        )}

        {open && step.kind === 'confirm-analysis' && (
          <PrefilledItemConfirmStep
            initialInput={imageAnalysisToItemInput(step.result)}
            suggestedCategoryName={step.result.objectType}
            images={[]}
            previewImageUrl={photoPreviewUrl}
            categories={categories}
            onCreateCategory={onCreateCategory}
            onCancel={handleClose}
            onSubmit={handleSubmit}
          />
        )}
      </DialogContent>
    </Dialog>
  )
}
