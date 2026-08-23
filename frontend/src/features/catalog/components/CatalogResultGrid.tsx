import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import { Box, Button, Card, CardContent, Stack, Typography } from '@mui/material'
import type { CatalogResult } from '../types/CatalogResult'

interface CatalogResultGridProps {
  results: CatalogResult[]
  onSelect: (result: CatalogResult) => void
}

export function CatalogResultGrid({ results, onSelect }: CatalogResultGridProps) {
  return (
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
        gap: 2,
      }}
    >
      {results.map((result) => (
        <Card
          key={`${result.manufacturer ?? ''}-${result.mpn ?? result.name}`}
          variant="outlined"
          sx={{ overflow: 'hidden', borderColor: 'divider', display: 'flex', flexDirection: 'column' }}
        >
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              aspectRatio: '4 / 3',
              bgcolor: 'background.default',
              color: 'text.disabled',
            }}
          >
            {result.images[0] ? (
              <Box
                component="img"
                src={result.images[0].url}
                alt={result.name}
                sx={{ width: '100%', height: '100%', objectFit: 'contain' }}
              />
            ) : (
              <ImageOutlinedIcon sx={{ fontSize: 48 }} />
            )}
          </Box>

          <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, flexGrow: 1 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
              {result.name}
            </Typography>
            {result.manufacturer && (
              <Typography variant="body2" color="textSecondary">
                {result.manufacturer}
              </Typography>
            )}
            {result.mpn && (
              <Typography variant="body2" color="textSecondary">
                {result.mpn}
              </Typography>
            )}

            {result.sources.length > 0 && (
              <Typography variant="caption" color="textSecondary" sx={{ mt: 0.5 }}>
                Sources : {result.sources.join(' • ')}
              </Typography>
            )}

            <Box sx={{ flexGrow: 1 }} />

            <Stack direction="row" sx={{ justifyContent: 'flex-end', mt: 1.5 }}>
              <Button variant="contained" size="small" onClick={() => onSelect(result)}>
                Sélectionner
              </Button>
            </Stack>
          </CardContent>
        </Card>
      ))}
    </Box>
  )
}
