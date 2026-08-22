import { useState } from 'react'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import { Box, Chip, Stack, Typography } from '@mui/material'
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

function toItemInput(result: CatalogResult): ItemInput {
  return {
    name: result.name,
    quantity: 1,
    manufacturer: result.manufacturer,
    reference: result.mpn,
    description: result.description,
    datasheetUrl: result.datasheetUrl,
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

  return (
    <Stack spacing={2.5}>
      {result.category && (
        <Typography variant="body2" color="textSecondary">
          Catégorie suggérée par le catalogue :{' '}
          <Chip component="span" label={result.category} size="small" variant="outlined" />
        </Typography>
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
        initialValue={toItemInput(result)}
        categories={categories}
        submitLabel="Ajouter"
        onCancel={onCancel}
        onCreateCategory={onCreateCategory}
        onSubmit={(input) =>
          onSubmit(
            input,
            selectedImage ? { url: selectedImage.url, provider: selectedImage.provider } : null,
          )
        }
      />
    </Stack>
  )
}
