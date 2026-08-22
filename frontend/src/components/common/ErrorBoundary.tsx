import { Component, type ErrorInfo, type ReactNode } from 'react'
import i18n from '@/i18n/config'

interface ErrorBoundaryProps {
  children: ReactNode
}

interface ErrorBoundaryState {
  hasError: boolean
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false }

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled UI error caught by ErrorBoundary:', error, info.componentStack)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-base-100 p-8">
          <div className="flex flex-col items-center gap-4 text-center">
            <h1 className="text-2xl font-bold">{i18n.t('errors.boundaryTitle')}</h1>
            <p className="text-base-content/70">{i18n.t('errors.boundaryMessage')}</p>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => window.location.reload()}
            >
              {i18n.t('errors.reload')}
            </button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
