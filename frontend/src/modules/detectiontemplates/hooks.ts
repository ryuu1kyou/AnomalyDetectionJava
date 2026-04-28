import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'
import type { CreateUpdateDetectionTemplateInput, DetectionTemplate } from './types'

const KEY = ['detection-templates']

export function useDetectionTemplates() {
  return useQuery({
    queryKey: KEY,
    queryFn: () => apiFetch<DetectionTemplate[]>('/app/detection-templates'),
  })
}

export function useCreateDetectionTemplate() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateUpdateDetectionTemplateInput) =>
      apiFetch<DetectionTemplate>('/app/detection-templates', {
        method: 'POST',
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useUpdateDetectionTemplate() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: CreateUpdateDetectionTemplateInput }) =>
      apiFetch<DetectionTemplate>(`/app/detection-templates/${id}`, {
        method: 'PUT',
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useDeleteDetectionTemplate() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<void>(`/app/detection-templates/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}
