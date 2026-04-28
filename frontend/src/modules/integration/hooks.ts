import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'
import type { CreateIntegrationEndpointInput, IntegrationEndpoint } from './types'

const KEY = ['integration-endpoints']

export function useIntegrationEndpoints() {
  return useQuery({
    queryKey: KEY,
    queryFn: () => apiFetch<IntegrationEndpoint[]>('/app/integration/endpoints'),
  })
}

export function useCreateIntegrationEndpoint() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateIntegrationEndpointInput) =>
      apiFetch<IntegrationEndpoint>('/app/integration/endpoints', {
        method: 'POST',
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useDeleteIntegrationEndpoint() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<void>(`/app/integration/endpoints/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useTestConnection() {
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<boolean>(`/app/integration/endpoints/${id}/test`, { method: 'POST' }),
  })
}
