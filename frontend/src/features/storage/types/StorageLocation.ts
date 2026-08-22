export interface StorageLocation {
  id: number
  name: string
  parentId: number | null
  hasChildren: boolean
}
