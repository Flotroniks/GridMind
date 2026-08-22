import { useMemo, useState, type ReactNode } from 'react'
import { CssBaseline, ThemeProvider } from '@mui/material'
import { ColorModeContext, type ColorMode } from './colorModeContext'
import { darkTheme, lightTheme } from './theme'

const STORAGE_KEY = 'gridmind-theme'

function getInitialMode(): ColorMode {
  if (typeof window === 'undefined') return 'dark'
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY)
    return stored === 'light' ? 'light' : 'dark'
  } catch {
    return 'dark'
  }
}

export function ColorModeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<ColorMode>(getInitialMode)

  const value = useMemo(
    () => ({
      mode,
      toggle: () => {
        setMode((current) => {
          const next: ColorMode = current === 'dark' ? 'light' : 'dark'
          try {
            window.localStorage.setItem(STORAGE_KEY, next)
          } catch {
            // ignore storage errors (e.g. private browsing)
          }
          return next
        })
      },
    }),
    [mode],
  )

  return (
    <ColorModeContext.Provider value={value}>
      <ThemeProvider theme={mode === 'dark' ? darkTheme : lightTheme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </ColorModeContext.Provider>
  )
}
