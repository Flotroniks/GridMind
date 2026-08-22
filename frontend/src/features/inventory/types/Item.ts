export interface Item {
  id: number
  name: string
  quantity: number
  description: string | null
  manufacturer: string | null
  reference: string | null
  categoryId: number | null
  categoryName: string | null
  tags: string[]
  notes: string | null
  productUrl: string | null
  datasheetUrl: string | null
  minimumQuantity: number
}

export interface ItemInput {
  name: string
  quantity: number
  description?: string | null
  manufacturer?: string | null
  reference?: string | null
  categoryId?: number | null
  tags?: string[]
  notes?: string | null
  productUrl?: string | null
  datasheetUrl?: string | null
  minimumQuantity?: number
}

export interface ItemFilters {
  search?: string
  categoryId?: number
  manufacturer?: string
}
