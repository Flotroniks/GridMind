import type { ItemInput } from '@/features/inventory/types/Item'
import type { ImageAnalysisResult } from '../types/ImageAnalysis'

export function toItemInput(result: ImageAnalysisResult): ItemInput {
  return {
    name: result.name ?? result.objectType ?? '',
    quantity: 1,
    manufacturer: result.manufacturer,
    reference: result.model,
    description: result.characteristics.length > 0 ? result.characteristics.join(', ') : null,
    notes: result.visibleText.length > 0 ? `Textes visibles : ${result.visibleText.join(', ')}` : null,
  }
}
