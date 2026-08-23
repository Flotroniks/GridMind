import { useEffect, useState } from 'react'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { Dialog, DialogContent, DialogTitle, IconButton, Stack } from '@mui/material'
import type { Category } from '@/features/categories/types/Category'
import { AnalyzeImageStep } from '@/features/imageanalysis/components/AnalyzeImageStep'
import type { ImageAnalysisResult } from '@/features/imageanalysis/types/ImageAnalysis'
import { suggestSearchQueries } from '@/features/imageanalysis/utils/suggestSearchQueries'
import { toItemInput as imageAnalysisToItemInput } from '@/features/imageanalysis/utils/toItemInput'
import {
  PrefilledItemConfirmStep,
  type CatalogImageSource,
} from '@/features/inventory/components/PrefilledItemConfirmStep'
import type { ItemInput } from '@/features/inventory/types/Item'
import { CatalogMatchPicker } from './CatalogMatchPicker'
import { CatalogSearchStep } from './CatalogSearchStep'
import type { CatalogResult } from '../types/CatalogResult'

interface ProductSearchModalProps {
  open: boolean
  categories: Category[]
  onClose: () => void
  onCreateCategory: (name: string) => Promise<Category>
  onSubmit: (input: ItemInput, source: CatalogImageSource | null) => Promise<void>
  onSubmitWithPhoto: (input: ItemInput, photo: File) => Promise<void>
  onManualCreateRequest: (query: string) => void
  /** Opens straight into a search for this term instead of an empty search step — used
   * when arriving here from a keyword the image-analysis feature suggested elsewhere
   * (e.g. the manual item form's "Remplir avec IA"). */
  initialSearchQuery?: string
  /** Opens straight into the confirm step for this already-picked result — used when the
   * manual item form's automatic catalog-match picker already found and selected one, so
   * there's no need to search again. */
  initialCatalogResult?: CatalogResult | null
}

type Step =
  | { kind: 'search'; initialQuery?: string }
  | { kind: 'analyze-photo' }
  | { kind: 'auto-catalog-match'; analysis: ImageAnalysisResult; queries: string[] }
  | { kind: 'confirm-catalog'; result: CatalogResult }
  | { kind: 'confirm-analysis'; result: ImageAnalysisResult }

const TITLES: Record<Step['kind'], string> = {
  search: 'Rechercher un produit',
  'analyze-photo': 'Analyser une photo',
  'auto-catalog-match': 'Correspondances trouvées',
  'confirm-catalog': "Confirmer l'objet",
  'confirm-analysis': "Confirmer l'objet",
}

export function ProductSearchModal({
  open,
  categories,
  onClose,
  onCreateCategory,
  onSubmit,
  onSubmitWithPhoto,
  onManualCreateRequest,
  initialSearchQuery,
  initialCatalogResult,
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

  useEffect(() => {
    if (open && initialSearchQuery) {
      setStep({ kind: 'search', initialQuery: initialSearchQuery })
    }
  }, [open, initialSearchQuery])

  useEffect(() => {
    if (open && initialCatalogResult) {
      setStep({ kind: 'confirm-catalog', result: initialCatalogResult })
    }
  }, [open, initialCatalogResult])

  const handleClose = () => {
    setStep({ kind: 'search' })
    setPhotoFile(null)
    onClose()
  }

  const handleSubmit = async (input: ItemInput, source: CatalogImageSource | null) => {
    await onSubmit(input, source)
    handleClose()
  }

  const handleSubmitWithPhoto = async (input: ItemInput, photo: File) => {
    await onSubmitWithPhoto(input, photo)
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
            initialQuery={step.initialQuery}
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
            onAnalyzed={(result) => {
              const queries = suggestSearchQueries(result)
              setStep(
                queries.length > 0
                  ? { kind: 'auto-catalog-match', analysis: result, queries }
                  : { kind: 'confirm-analysis', result },
              )
            }}
          />
        )}

        {open && step.kind === 'auto-catalog-match' && (
          <CatalogMatchPicker
            queries={step.queries}
            onSelect={(result) => setStep({ kind: 'confirm-catalog', result })}
            onSkip={() => setStep({ kind: 'confirm-analysis', result: step.analysis })}
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
            photoFile={photoFile}
            onSubmitWithPhoto={handleSubmitWithPhoto}
            searchSuggestions={step.result.searchQueries}
            onSearchCatalogRequest={(query) => setStep({ kind: 'search', initialQuery: query })}
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
