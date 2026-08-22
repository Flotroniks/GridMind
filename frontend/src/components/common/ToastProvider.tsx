import { useCallback, useState, type ReactNode } from 'react'
import { Alert, Snackbar, Stack } from '@mui/material'
import { ToastContext, type ToastVariant } from './toastContext'

interface Toast {
  id: number
  message: string
  variant: ToastVariant
}

let nextId = 0

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback(
    (message: string, variant: ToastVariant = 'info') => {
      const id = nextId++
      setToasts((current) => [...current, { id, message, variant }])
      setTimeout(() => dismiss(id), 4000)
    },
    [dismiss],
  )

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <Stack
        spacing={1}
        sx={{ position: 'fixed', bottom: 16, right: 16, zIndex: (theme) => theme.zIndex.snackbar }}
      >
        {toasts.map((toast) => (
          <Snackbar key={toast.id} open sx={{ position: 'static' }}>
            <Alert severity={toast.variant} variant="filled" onClose={() => dismiss(toast.id)}>
              {toast.message}
            </Alert>
          </Snackbar>
        ))}
      </Stack>
    </ToastContext.Provider>
  )
}
