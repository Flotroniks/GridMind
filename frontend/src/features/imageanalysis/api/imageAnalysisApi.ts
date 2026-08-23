import { postMultipart } from '@/lib/apiClient'
import type { ImageAnalysisResult } from '../types/ImageAnalysis'

const BASE_PATH = '/api/image-analysis'

export function analyzeImage(file: File): Promise<ImageAnalysisResult> {
  const formData = new FormData()
  formData.append('image', file)
  return postMultipart<ImageAnalysisResult>(`${BASE_PATH}/analyze`, formData)
}
