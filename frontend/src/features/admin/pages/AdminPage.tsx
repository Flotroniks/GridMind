import { useState } from 'react'
import { Box, Card, CardContent, Tab, Tabs, Typography } from '@mui/material'
import { CategoryManager } from '../components/CategoryManager'
import { MqttLiveFeed } from '../components/MqttLiveFeed'
import { SystemStatusPanel } from '../components/SystemStatusPanel'

const TABS = ['status', 'categories', 'mqtt'] as const
type Tab = (typeof TABS)[number]

const TAB_LABELS: Record<Tab, string> = {
  status: 'Statut',
  categories: 'Catégories',
  mqtt: 'MQTT en direct',
}

export function AdminPage() {
  const [tab, setTab] = useState<Tab>('status')

  return (
    <Box sx={{ mx: 'auto', width: { xs: '100%', sm: '75vw' }, maxWidth: 1200 }}>
      <Card variant="outlined" sx={{ backdropFilter: 'blur(8px)', borderColor: 'divider' }}>
        <CardContent sx={{ py: 3, px: { xs: 3, sm: 5, md: 8 } }}>
          <Typography variant="overline" color="primary" sx={{ fontWeight: 600, letterSpacing: 2 }}>
            GridMind
          </Typography>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 3 }}>
            Administration
          </Typography>

          <Tabs
            value={tab}
            onChange={(_event, value: Tab) => setTab(value)}
            sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
          >
            {TABS.map((value) => (
              <Tab key={value} value={value} label={TAB_LABELS[value]} />
            ))}
          </Tabs>

          {tab === 'status' && <SystemStatusPanel />}
          {tab === 'categories' && <CategoryManager />}
          {tab === 'mqtt' && <MqttLiveFeed />}
        </CardContent>
      </Card>
    </Box>
  )
}
