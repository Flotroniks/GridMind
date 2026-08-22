import { AppBar, Box, Button, Stack, Toolbar, Typography } from '@mui/material'
import { NavLink, Outlet } from 'react-router'
import { LanguageSwitcher } from '@/components/common/LanguageSwitcher'
import { ThemeToggle } from '@/components/common/ThemeToggle'

export function MainLayout() {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: (theme) =>
          theme.palette.mode === 'dark'
            ? `linear-gradient(160deg, ${theme.palette.background.paper}, ${theme.palette.background.default})`
            : `linear-gradient(160deg, ${theme.palette.grey[100]}, ${theme.palette.background.default})`,
      }}
    >
      <AppBar
        position="static"
        color="transparent"
        elevation={0}
        sx={{ borderBottom: 1, borderColor: 'divider', backdropFilter: 'blur(8px)' }}
      >
        <Toolbar sx={{ gap: 2, px: { xs: 2, sm: 4 } }}>
          <Typography variant="h6" component="span" color="primary" sx={{ flexGrow: 1, fontWeight: 700 }}>
            GridMind
          </Typography>
          <Stack direction="row" spacing={1}>
            <Button
              component={NavLink}
              to="/"
              end
              color="inherit"
              sx={{ '&.active': { bgcolor: 'action.selected' } }}
            >
              Inventaire
            </Button>
            <Button
              component={NavLink}
              to="/storage"
              color="inherit"
              sx={{ '&.active': { bgcolor: 'action.selected' } }}
            >
              Stockage
            </Button>
          </Stack>
          <LanguageSwitcher />
          <ThemeToggle />
        </Toolbar>
      </AppBar>
      <Box component="main" sx={{ p: { xs: 2, sm: 4 } }}>
        <Outlet />
      </Box>
    </Box>
  )
}
