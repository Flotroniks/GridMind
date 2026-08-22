import { useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
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
    <form className="flex gap-2" onSubmit={handleSubmit}>
      <input
        type="text"
        className="input input-bordered input-sm flex-1"
        placeholder="Nouvel emplacement…"
        value={name}
        onChange={(event) => setName(event.target.value)}
        autoComplete="off"
      />
      <button type="submit" className="btn btn-sm btn-primary" disabled={mutation.isPending}>
        Ajouter
      </button>
    </form>
  )
}
