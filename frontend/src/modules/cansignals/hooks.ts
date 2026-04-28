import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'
import type { CanSignal, CreateUpdateCanSignalInput } from './types'

const KEY = ['can-signals']

export function useCanSignals() {
  return useQuery({
    queryKey: KEY,
    queryFn: () => apiFetch<CanSignal[]>('/app/can-signals'),
  })
}

export function useCreateCanSignal() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateUpdateCanSignalInput) =>
      apiFetch<CanSignal>('/app/can-signals', {
        method: 'POST',
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useUpdateCanSignal() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: CreateUpdateCanSignalInput }) =>
      apiFetch<CanSignal>(`/app/can-signals/${id}`, {
        method: 'PUT',
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useDeleteCanSignal() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<void>(`/app/can-signals/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}
