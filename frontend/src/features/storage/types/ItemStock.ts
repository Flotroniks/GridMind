export interface ItemStock {
  itemId: number
  itemName: string
  storageLocationId: number
  storageLocationName: string | null
  quantity: number
}
