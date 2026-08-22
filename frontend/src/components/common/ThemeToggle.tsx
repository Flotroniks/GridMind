import { IconButton } from '@mui/material'
import DarkModeIcon from '@mui/icons-material/DarkMode'
import LightModeIcon from '@mui/icons-material/LightMode'
import { useColorMode } from '@/theme/useColorMode'

export function ThemeToggle() {
  const { mode, toggle } = useColorMode()

  return (
    <IconButton size="small" onClick={toggle} aria-label="Toggle theme" color="inherit">
      {mode === 'dark' ? <LightModeIcon fontSize="small" /> : <DarkModeIcon fontSize="small" />}
    </IconButton>
  )
}
