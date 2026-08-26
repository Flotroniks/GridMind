export interface StorageLocation {
  id: number
  name: string
  parentId: number | null
  hasChildren: boolean
  ledControllerId: string | null
  ledIndex: number | null
}
