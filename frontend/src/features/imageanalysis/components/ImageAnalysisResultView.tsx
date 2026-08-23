import type { ReactNode } from 'react'
import { Chip, Stack, Typography } from '@mui/material'
import type { ImageAnalysisResult } from '../types/ImageAnalysis'

interface ImageAnalysisResultViewProps {
  result: ImageAnalysisResult
}

function Section({ label, children }: { label: string; children: ReactNode }) {
  return (
    <Stack spacing={0.5}>
      <Typography variant="overline" color="textSecondary" sx={{ letterSpacing: 1 }}>
        {label}
      </Typography>
      {children}
    </Stack>
  )
}

export function ImageAnalysisResultView({ result }: ImageAnalysisResultViewProps) {
  const hasIdentity = Boolean(result.name || result.manufacturer)
  const hasAnything =
    hasIdentity ||
    result.objectType ||
    result.model ||
    result.confidence != null ||
    result.visibleText.length > 0 ||
    result.characteristics.length > 0 ||
    result.searchQueries.length > 0

  if (!hasAnything) {
    return (
      <Typography color="textSecondary">
        Aucune information exploitable identifiée. Essayez une image plus nette ou plus proche de l'objet.
      </Typography>
    )
  }

  return (
    <Stack spacing={2.5}>
      {hasIdentity && (
        <Stack spacing={0.25}>
          {result.name && (
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              {result.name}
            </Typography>
          )}
          {result.manufacturer && (
            <Typography variant="subtitle1" color="textSecondary">
              {result.manufacturer}
            </Typography>
          )}
        </Stack>
      )}

      <Stack direction="row" spacing={4} sx={{ flexWrap: 'wrap', rowGap: 2 }}>
        {result.objectType && (
          <Section label="Type">
            <Typography>{result.objectType}</Typography>
          </Section>
        )}
        {result.model && (
          <Section label="Modèle probable">
            <Typography>{result.model}</Typography>
          </Section>
        )}
        {result.confidence != null && (
          <Section label="Confiance">
            <Typography>{Math.round(result.confidence * 100)} %</Typography>
          </Section>
        )}
      </Stack>

      {result.visibleText.length > 0 && (
        <Section label="Textes visibles">
          <Stack spacing={0.5}>
            {result.visibleText.map((text) => (
              <Typography key={text} variant="body2">
                • {text}
              </Typography>
            ))}
          </Stack>
        </Section>
      )}

      {result.characteristics.length > 0 && (
        <Section label="Caractéristiques détectées">
          <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1 }}>
            {result.characteristics.map((characteristic) => (
              <Chip key={characteristic} label={characteristic} size="small" variant="outlined" />
            ))}
          </Stack>
        </Section>
      )}

      {result.searchQueries.length > 0 && (
        <Section label="Suggestions de recherche">
          <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1 }}>
            {result.searchQueries.map((query) => (
              <Chip key={query} label={query} size="small" />
            ))}
          </Stack>
        </Section>
      )}
    </Stack>
  )
}
