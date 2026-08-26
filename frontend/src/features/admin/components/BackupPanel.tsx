import { useRef, useState } from 'react'
import DeleteForeverOutlinedIcon from '@mui/icons-material/DeleteForeverOutlined'
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Typography,
} from '@mui/material'
import { useToast } from '@/components/common/useToast'
import { ApiError } from '@/lib/apiClient'
import * as adminApi from '../api/adminApi'
import type { BackupSummary } from '../types/Backup'

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback
}

function summaryText(summary: BackupSummary): string {
  return (
    `${summary.categories} catégorie(s), ${summary.storageLocations} emplacement(s), ` +
    `${summary.items} objet(s), ${summary.images} image(s), ${summary.itemStock} stock(s) restauré(s).`
  )
}

export function BackupPanel() {
  const { showToast } = useToast()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [pendingImportFile, setPendingImportFile] = useState<File | null>(null)
  const [confirmingClear, setConfirmingClear] = useState(false)
  const [busy, setBusy] = useState(false)

  const handleExport = () => {
    const link = document.createElement('a')
    link.href = adminApi.exportBackupUrl()
    link.click()
  }

  const handleFileSelected = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (file) setPendingImportFile(file)
  }

  const performImport = async () => {
    if (!pendingImportFile) return
    setBusy(true)
    try {
      const text = await pendingImportFile.text()
      const summary = await adminApi.importBackup(text)
      setPendingImportFile(null)
      showToast(`Import terminé : ${summaryText(summary)}`, 'success')
      // Virtually every page's data just changed underneath it — a full reload is
      // simpler and more reliable here than trying to invalidate every query cache.
      window.location.reload()
    } catch (error) {
      showToast(messageOf(error, "Échec de l'import."), 'error')
    } finally {
      setBusy(false)
    }
  }

  const performClear = async () => {
    setBusy(true)
    try {
      await adminApi.clearBackup()
      setConfirmingClear(false)
      showToast('Base de données vidée.', 'success')
      window.location.reload()
    } catch (error) {
      showToast(messageOf(error, 'Effacement impossible.'), 'error')
    } finally {
      setBusy(false)
    }
  }

  return (
    <Stack spacing={2.5}>
      <Typography variant="body2" color="textSecondary">
        Exporte tout le contenu de GridMind — catégories, emplacements (y compris leur
        mappage LED), objets et leurs images, stocks — dans un fichier JSON autonome. Ce
        fichier peut être réimporté ici même, ou sur une autre installation.
      </Typography>

      <Stack direction="row" spacing={1.5} sx={{ flexWrap: 'wrap' }}>
        <Button variant="outlined" startIcon={<DownloadOutlinedIcon />} onClick={handleExport} disabled={busy}>
          Exporter
        </Button>
        <Button
          variant="outlined"
          startIcon={<UploadFileOutlinedIcon />}
          onClick={() => fileInputRef.current?.click()}
          disabled={busy}
        >
          Importer
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          accept="application/json"
          hidden
          onChange={handleFileSelected}
        />
        <Button
          variant="outlined"
          color="error"
          startIcon={<DeleteForeverOutlinedIcon />}
          onClick={() => setConfirmingClear(true)}
          disabled={busy}
          sx={{ ml: { sm: 'auto' } }}
        >
          Vider la base de données
        </Button>
      </Stack>

      <Dialog open={pendingImportFile !== null} onClose={() => setPendingImportFile(null)}>
        <DialogTitle>Importer une sauvegarde</DialogTitle>
        <DialogContent>
          <Stack spacing={2}>
            <Alert severity="warning">
              L'import remplace entièrement les données actuelles — catégories,
              emplacements, objets, images et stocks existants seront supprimés avant la
              restauration.
            </Alert>
            <Typography variant="body2" color="textSecondary">
              Fichier sélectionné : <strong>{pendingImportFile?.name}</strong>
            </Typography>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPendingImportFile(null)} disabled={busy}>
            Annuler
          </Button>
          <Button color="warning" variant="contained" onClick={() => void performImport()} disabled={busy}>
            Remplacer et importer
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={confirmingClear} onClose={() => setConfirmingClear(false)}>
        <DialogTitle>Vider la base de données</DialogTitle>
        <DialogContent>
          <Alert severity="error">
            Ceci supprime définitivement toutes les catégories, tous les emplacements,
            tous les objets et tous les stocks. Cette action est irréversible — exportez
            d'abord une sauvegarde si vous n'êtes pas sûr.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmingClear(false)} disabled={busy}>
            Annuler
          </Button>
          <Button color="error" variant="contained" onClick={() => void performClear()} disabled={busy}>
            Vider définitivement
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  )
}
