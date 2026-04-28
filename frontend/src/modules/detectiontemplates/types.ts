export interface DetectionTemplate {
  id: string
  tenantId: string
  name: string
  description: string
  canSignalId: string
  expression: string
  threshold: number | null
  isActive: boolean
}

export interface CreateUpdateDetectionTemplateInput {
  name: string
  description: string
  canSignalId: string
  expression: string
  threshold: number | null
  isActive: boolean
}
