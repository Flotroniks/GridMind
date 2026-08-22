import { createTheme, type ThemeOptions } from '@mui/material/styles'

const shared: ThemeOptions = {
  typography: {
    fontFamily: 'Inter, "Segoe UI", sans-serif',
  },
  shape: {
    borderRadius: 10,
  },
}

export const lightTheme = createTheme({
  ...shared,
  palette: {
    mode: 'light',
    primary: { main: '#4f46e5' },
    error: { main: '#dc2626' },
    background: { default: '#f3f4f6', paper: '#ffffff' },
  },
})

export const darkTheme = createTheme({
  ...shared,
  palette: {
    mode: 'dark',
    primary: { main: '#818cf8' },
    error: { main: '#f87171' },
    background: { default: '#191a1c', paper: '#202124' },
    divider: 'rgba(255, 255, 255, 0.12)',
  },
})
