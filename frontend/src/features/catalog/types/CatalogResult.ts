export interface CatalogImage {
  url: string
  provider: string
}

export interface CatalogResult {
  name: string
  manufacturer: string | null
  mpn: string | null
  description: string | null
  category: string | null
  datasheetUrl: string | null
  images: CatalogImage[]
  sources: string[]
}
