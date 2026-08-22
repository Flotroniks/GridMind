import { useEffect, useState } from 'react'

const STORAGE_KEY = 'gridmind-theme'

function getInitialTheme(): 'night' | 'light' {
  if (typeof document === 'undefined') return 'night'
  const current = document.documentElement.getAttribute('data-theme')
  return current === 'light' ? 'light' : 'night'
}

export function ThemeToggle() {
  const [theme, setTheme] = useState<'night' | 'light'>(getInitialTheme)

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    try {
      localStorage.setItem(STORAGE_KEY, theme)
    } catch {
      // ignore storage errors (e.g. private browsing)
    }
  }, [theme])

  return (
    <button
      type="button"
      className="btn btn-ghost btn-sm"
      onClick={() => setTheme((current) => (current === 'night' ? 'light' : 'night'))}
      aria-label="Toggle theme"
    >
      {theme === 'night' ? '☀️' : '🌙'}
    </button>
  )
}
