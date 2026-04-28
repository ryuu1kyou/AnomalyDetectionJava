import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'
import type {
  AnomalyDetectionLogic,
  CreateUpdateAnomalyDetectionLogicInput,
  DetectionLogicStatus,
} from './types'

const KEY = ['anomaly-detection-logics']

export function useAnomalyDetectionLogics(status?: DetectionLogicStatus) {
  return useQuery({
    queryKey: status ? [...KEY, status] : KEY,
    queryFn: () => {
      const qs = status ? `?status=${status}` : ''
      return apiFetch<AnomalyDetectionLogic[]>(`/app/can-anomaly-detection-logics${qs}`)
    },
  })
}

export function useCreateAnomalyDetectionLogic() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateUpdateAnomalyDetectionLogicInput) =>
      apiFetch<AnomalyDetectionLogic>('/app/can-anomaly-detection-logics', {
        method: 'POST',
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useUpdateAnomalyDetectionLogic() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({
      id,
      input,
    }: {
      id: string
      input: CreateUpdateAnomalyDetectionLogicInput
    }) =>
      apiFetch<AnomalyDetectionLogic>(`/app/can-anomaly-detection-logics/${id}`, {
        method: 'PUT',
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useDeleteAnomalyDetectionLogic() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<void>(`/app/can-anomaly-detection-logics/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useSubmitForApproval() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      apiFetch<AnomalyDetectionLogic>(
        `/app/can-anomaly-detection-logics/${id}/submit-for-approval`,
        { method: 'POST' }
      ),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useApproveLogic() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes?: string }) => {
      const qs = notes ? `?notes=${encodeURIComponent(notes)}` : ''
      return apiFetch<AnomalyDetectionLogic>(
        `/app/can-anomaly-detection-logics/${id}/approve${qs}`,
        { method: 'POST' }
      )
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useRejectLogic() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      apiFetch<AnomalyDetectionLogic>(
        `/app/can-anomaly-detection-logics/${id}/reject?reason=${encodeURIComponent(reason)}`,
        { method: 'POST' }
      ),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}
