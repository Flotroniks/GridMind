import { useRef, useState } from 'react'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import UploadFileIcon from '@mui/icons-material/UploadFile'
import { Alert, Box, Button, CircularProgress, Stack, Typography } from '@mui/material'
import { ApiError } from '@/lib/apiClient'
import * as imageAnalysisApi from '../api/imageAnalysisApi'
import type { ImageAnalysisResult } from '../types/ImageAnalysis'

const ACCEPTED_TYPES = 'image/jpeg,image/png'

interface AnalyzeImageStepProps {
  file: File | null
  previewUrl: string | null
  onFileSelected: (file: File | null) => void
  onAnalyzed: (result: ImageAnalysisResult) => void
}

function messageOf(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message
  }
  return 'Le serveur GridMind est injoignable. Vérifiez que le backend est démarré.'
}

// The object URL's lifecycle (create/revoke) is owned by the parent (ProductSearchModal),
// not this component: this step unmounts as soon as analysis succeeds and the flow moves
// to the confirm step, which still needs that same URL to keep showing the preview. Owning
// revocation here would invalidate the URL out from under the very next step.
export function AnalyzeImageStep({ file, previewUrl, onFileSelected, onAnalyzed }: AnalyzeImageStepProps) {
  const [analyzing, setAnalyzing] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileSelected = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selected = event.target.files?.[0] ?? null
    event.target.value = ''
    onFileSelected(selected)
    setErrorMessage(null)
  }

  const handleAnalyze = async () => {
    if (!file) return
    setAnalyzing(true)
    setErrorMessage(null)
    try {
      const result = await imageAnalysisApi.analyzeImage(file)
      onAnalyzed(result)
    } catch (analyzeError) {
      setErrorMessage(messageOf(analyzeError))
    } finally {
      setAnalyzing(false)
    }
  }

  return (
    <Stack spacing={2.5}>
      <Typography variant="body2" color="textSecondary">
        Identification locale par IA (expérimental) — photographiez l'objet, l'analyse pré-remplira
        le formulaire d'ajout que vous pourrez corriger avant de valider.
      </Typography>

      <input ref={fileInputRef} type="file" accept={ACCEPTED_TYPES} hidden onChange={handleFileSelected} />

      <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
        <Button
          variant="outlined"
          startIcon={<UploadFileIcon />}
          onClick={() => fileInputRef.current?.click()}
          disabled={analyzing}
        >
          Sélectionner une image
        </Button>
        {file && (
          <Typography variant="body2" color="textSecondary">
            {file.name}
          </Typography>
        )}
      </Stack>

      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          aspectRatio: '4 / 3',
          maxHeight: 320,
          bgcolor: 'background.default',
          border: '1px dashed',
          borderColor: 'divider',
          borderRadius: 2,
          color: 'text.disabled',
          overflow: 'hidden',
        }}
      >
        {previewUrl ? (
          <Box
            component="img"
            src={previewUrl}
            alt="Aperçu de l'image sélectionnée"
            sx={{ width: '100%', height: '100%', objectFit: 'contain' }}
          />
        ) : (
          <Stack spacing={1} sx={{ alignItems: 'center' }}>
            <ImageOutlinedIcon sx={{ fontSize: 48 }} />
            <Typography variant="body2">Aucune image sélectionnée</Typography>
          </Stack>
        )}
      </Box>

      <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
        <Button variant="contained" disabled={!file || analyzing} onClick={() => void handleAnalyze()}>
          Analyser
        </Button>
      </Stack>

      {analyzing && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: 'text.secondary' }}>
          <CircularProgress size={16} />
          <Typography color="textSecondary">
            Analyse en cours… cela peut prendre quelques secondes à quelques dizaines de secondes sur
            CPU.
          </Typography>
        </Stack>
      )}

      {errorMessage && <Alert severity="error">{errorMessage}</Alert>}
    </Stack>
  )
}
