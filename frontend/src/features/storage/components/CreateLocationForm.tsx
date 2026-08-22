import { useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Button, Stack, TextField } from '@mui/material'
import { useToast } from '@/components/common/useToast'
import { ApiError } from '@/lib/apiClient'
import * as storageApi from '../api/storageApi'

interface CreateLocationFormProps {
  parentId: number | null
}

export function CreateLocationForm({ parentId }: CreateLocationFormProps) {
  const [name, setName] = useState('')
  const { showToast } = useToast()
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => storageApi.createLocation(name.trim(), parentId),
    onSuccess: () => {
      setName('')
      showToast('Emplacement créé.', 'success')
      void queryClient.invalidateQueries({ queryKey: ['storage', 'children', parentId] })
    },
    onError: (error: unknown) => {
      const message = error instanceof ApiError || error instanceof Error ? error.message : 'Création impossible.'
      showToast(message, 'error')
    },
  })

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!name.trim()) return
    mutation.mutate()
  }

  return (
    <Stack component="form" direction="row" spacing={1} onSubmit={handleSubmit}>
      <TextField
        size="small"
        placeholder="Nouvel emplacement…"
        value={name}
        onChange={(event) => setName(event.target.value)}
        autoComplete="off"
        fullWidth
      />
      <Button
        type="submit"
        variant="contained"
        disabled={mutation.isPending}
        sx={{ whiteSpace: 'nowrap', flexShrink: 0 }}
      >
        Ajouter
      </Button>
    </Stack>
  )
}
