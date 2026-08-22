import { useCallback, useState, type ReactNode } from 'react'
import { ToastContext, type ToastVariant } from './toastContext'

interface Toast {
  id: number
  message: string
  variant: ToastVariant
}

let nextId = 0

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const showToast = useCallback((message: string, variant: ToastVariant = 'info') => {
    const id = nextId++
    setToasts((current) => [...current, { id, message, variant }])
    setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== id))
    }, 4000)
  }, [])

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="toast toast-end toast-bottom z-50">
        {toasts.map((toast) => (
          <div key={toast.id} role="alert" className={`alert alert-${toast.variant}`}>
            <span>{toast.message}</span>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
