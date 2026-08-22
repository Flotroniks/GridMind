export type ItemStatus = 'IN_SERVICE' | 'OUT_OF_SERVICE'

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
  status: ItemStatus
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
  status?: ItemStatus
}

export interface ItemFilters {
  search?: string
  categoryId?: number
  manufacturer?: string
  status?: ItemStatus
}
