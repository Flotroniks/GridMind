import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { ToastProvider } from '@/components/common/ToastProvider'
import { ColorModeProvider } from '@/theme/ColorModeProvider'
import { router } from './router'

const queryClient = new QueryClient()

function App() {
  return (
    <ColorModeProvider>
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <RouterProvider router={router} />
        </ToastProvider>
      </QueryClientProvider>
    </ColorModeProvider>
  )
}

export default App
