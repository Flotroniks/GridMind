export interface ImageAnalysisResult {
  objectType: string | null
  name: string | null
  manufacturer: string | null
  model: string | null
  visibleText: string[]
  characteristics: string[]
  confidence: number | null
  searchQueries: string[]
}
