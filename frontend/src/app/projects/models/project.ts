export enum ProjectStatus {
  Planning = 0,
  Active = 1,
  OnHold = 2,
  Completed = 3,
  Cancelled = 4,
}

export enum ProjectPriority {
  Low = 1,
  Medium = 2,
  High = 3,
  Critical = 4,
}

export interface Project {
  id: string
  projectCode: string
  projectName: string
  description: string

  vehicleModel: string
  modelYear: string
  platform: string

  primarySystem: string
  targetMarket: string
  status: ProjectStatus
  priority: ProjectPriority

  startDate: string
  plannedEndDate: string
  actualEndDate?: string

  progressPercentage: number

  oemCode: string
  oemName: string

  totalDetectionLogics: number
  totalCanSignals: number
  totalAnomalies: number
  resolvedAnomalies: number
}

export interface ProjectsQuery {
  filter?: string
  status?: ProjectStatus
  priority?: ProjectPriority
  vehicleModel?: string
  primarySystem?: string
}
