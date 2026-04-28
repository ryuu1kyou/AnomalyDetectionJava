export type AnomalyType =
  | 'TIMEOUT'
  | 'OUT_OF_RANGE'
  | 'RATE_OF_CHANGE'
  | 'STUCK'
  | 'PERIODIC_ANOMALY'
  | 'DATA_LOSS'
  | 'NOISE'
  | 'PATTERN_ANOMALY'
  | 'CORRELATION_ANOMALY'
  | 'CUSTOM'

export type AsilLevel = 'QM' | 'A' | 'B' | 'C' | 'D'

export type DetectionLogicStatus =
  | 'DRAFT'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'DEPRECATED'

export type LogicComplexity = 'SIMPLE' | 'MEDIUM' | 'COMPLEX'

export type ImplementationType =
  | 'CONFIGURATION'
  | 'SCRIPT'
  | 'SOURCE_CODE'
  | 'COMPILED_CODE'
  | 'TEMPLATE'

export type SharingLevel = 'PRIVATE' | 'OEM_PARTNER' | 'INDUSTRY' | 'PUBLIC'

export interface AnomalyDetectionLogic {
  id: string
  tenantId: string
  name: string
  version: string
  oemCode: string
  anomalyType: AnomalyType
  description: string
  targetSystemType: string
  complexity: LogicComplexity
  requirements: string
  implementationType: ImplementationType
  implementationLanguage: string
  asilLevel: AsilLevel
  safetyRequirementId: string
  safetyGoalId: string
  status: DetectionLogicStatus
  sharingLevel: SharingLevel
  vehiclePhaseId: string | null
  approvedAt: string | null
  approvedBy: string | null
  approvalNotes: string
  executionCount: number
  lastExecutedAt: string | null
  lastExecutionTimeMs: number | null
}

export interface CreateUpdateAnomalyDetectionLogicInput {
  name: string
  version: string
  oemCode: string
  anomalyType: AnomalyType
  description: string
  targetSystemType: string
  complexity: LogicComplexity
  requirements: string
  implementationType: ImplementationType
  implementationContent: string
  implementationLanguage: string
  implementationEntryPoint: string
  asilLevel: AsilLevel
  safetyRequirementId: string
  safetyGoalId: string
  hazardAnalysisId: string
  sharingLevel: SharingLevel
  vehiclePhaseId: string | null
}
