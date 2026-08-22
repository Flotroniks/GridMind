import { Box, Button, Stack, Typography } from '@mui/material'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

export function NotFoundPage() {
  const { t } = useTranslation()

  return (
    <Box sx={{ mx: 'auto', width: '100%', maxWidth: 448, textAlign: 'center' }}>
      <Stack spacing={2} sx={{ alignItems: 'center' }}>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>
          {t('notFoundPage.title')}
        </Typography>
        <Typography color="text.secondary">{t('notFoundPage.message')}</Typography>
        <Button component={Link} to="/" variant="contained">
          {t('notFoundPage.backHome')}
        </Button>
      </Stack>
    </Box>
  )
}
