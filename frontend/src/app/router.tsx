import { createBrowserRouter } from 'react-router'
import { MainLayout } from '@/layouts/MainLayout'
import { ImageAnalysisPage } from '@/features/imageanalysis/pages/ImageAnalysisPage'
import { InventoryPage } from '@/features/inventory/pages/InventoryPage'
import { ItemDetailsPage } from '@/features/inventory/pages/ItemDetailsPage'
import { StorageHierarchyPage } from '@/features/storage/pages/StorageHierarchyPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true, element: <InventoryPage /> },
      { path: 'inventory/:id', element: <ItemDetailsPage /> },
      { path: 'storage', element: <StorageHierarchyPage /> },
      { path: 'image-analysis', element: <ImageAnalysisPage /> },
    ],
  },
])
