import { useEffect, useRef, useState } from 'react'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import UploadFileIcon from '@mui/icons-material/UploadFile'
import { Alert, Box, Button, Card, CardContent, CircularProgress, Divider, Stack, Typography } from '@mui/material'
import { ApiError } from '@/lib/apiClient'
import * as imageAnalysisApi from '../api/imageAnalysisApi'
import { ImageAnalysisResultView } from '../components/ImageAnalysisResultView'
import type { ImageAnalysisResult } from '../types/ImageAnalysis'

type Status = 'idle' | 'analyzing' | 'success' | 'error'

const ACCEPTED_TYPES = 'image/jpeg,image/png'

function messageOf(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message
  }
  return 'Le serveur GridMind est injoignable. Vérifiez que le backend est démarré.'
}

export function ImageAnalysisPage() {
  const [file, setFile] = useState<File | null>(null)
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  const [status, setStatus] = useState<Status>('idle')
  const [result, setResult] = useState<ImageAnalysisResult | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (!file) {
      setPreviewUrl(null)
      return
    }
    const url = URL.createObjectURL(file)
    setPreviewUrl(url)
    return () => URL.revokeObjectURL(url)
  }, [file])

  const handleFileSelected = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selected = event.target.files?.[0] ?? null
    event.target.value = ''
    setFile(selected)
    setStatus('idle')
    setResult(null)
    setErrorMessage(null)
  }

  const handleAnalyze = async () => {
    if (!file) return
    setStatus('analyzing')
    setErrorMessage(null)
    try {
      const analyzed = await imageAnalysisApi.analyzeImage(file)
      setResult(analyzed)
      setStatus('success')
    } catch (analyzeError) {
      setErrorMessage(messageOf(analyzeError))
      setStatus('error')
    }
  }

  const analyzing = status === 'analyzing'

  return (
    <Box sx={{ mx: 'auto', width: { xs: '100%', sm: '75vw' }, maxWidth: 900 }}>
      <Card variant="outlined" sx={{ backdropFilter: 'blur(8px)', borderColor: 'divider' }}>
        <CardContent sx={{ py: 3, px: { xs: 3, sm: 5 } }}>
          <Box sx={{ mb: 3 }}>
            <Typography variant="overline" color="primary" sx={{ fontWeight: 600, letterSpacing: 2 }}>
              GridMind
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>
              Analyse d'un produit (IA locale)
            </Typography>
            <Typography variant="body2" color="textSecondary" sx={{ mt: 0.5 }}>
              Prototype expérimental — identification d'un objet à partir d'une photo, via un modèle
              vision exécuté localement (Ollama). Rien n'est envoyé à un service externe, rien n'est
              enregistré dans l'inventaire.
            </Typography>
          </Box>

          <Stack spacing={2.5}>
            <input
              ref={fileInputRef}
              type="file"
              accept={ACCEPTED_TYPES}
              hidden
              onChange={handleFileSelected}
            />

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
                maxHeight: 360,
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
              <Button
                variant="contained"
                disabled={!file || analyzing}
                onClick={() => void handleAnalyze()}
              >
                Analyser
              </Button>
            </Stack>

            {analyzing && (
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: 'text.secondary' }}>
                <CircularProgress size={16} />
                <Typography color="textSecondary">
                  Analyse en cours… cela peut prendre quelques secondes à quelques dizaines de
                  secondes sur CPU.
                </Typography>
              </Stack>
            )}

            {status === 'error' && errorMessage && <Alert severity="error">{errorMessage}</Alert>}

            {status === 'success' && result && (
              <>
                <Divider />
                <ImageAnalysisResultView result={result} />
              </>
            )}
          </Stack>
        </CardContent>
      </Card>
    </Box>
  )
}
