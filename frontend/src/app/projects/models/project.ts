export enum ProjectStatus {
  Planning = 0,
  Active = 1,
  OnHold = 2,
  Completed = 3,
  Cancelled = 4,
}

export enum MilestoneStatus {
  NotStarted = 0,
  InProgress = 1,
  Completed = 2,
  Delayed = 3,
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

export interface ProjectMilestone {
  id: string
  projectId: string
  name: string
  description: string
  plannedDate: string
  actualDate?: string
  status: MilestoneStatus
  progressPercentage: number
  dependencies: string[]
  deliverables: string[]
}

export interface ProjectMember {
  id: string
  projectId: string
  userId: string
  userName: string
  email: string
  role: string
  responsibilities: string[]
  joinedDate: string
  leftDate?: string
  isActive: boolean
  canEdit: boolean
  canDelete: boolean
  canManageMembers: boolean
}

export interface PagedResult<T> {
  items: T[]
  totalCount: number
}

export interface ProjectsQuery {
  filter?: string
  status?: ProjectStatus
  priority?: ProjectPriority
  vehicleModel?: string
  primarySystem?: string
}

/**
 * Backend-compatible list query (ABP GetList style)
 * - skipCount/maxResultCount: pagination
 * - sorting: e.g. "projectCode asc" / "creationTime desc"
 */
export interface GetProjectsInput extends ProjectsQuery {
  skipCount?: number
  maxResultCount?: number
  sorting?: string
}

export interface CreateProjectMilestoneDto {
  projectId: string
  name: string
  description: string
  plannedDate: string
  dependencies: string[]
  deliverables: string[]
}

export interface UpdateProjectMilestoneDto {
  name: string
  description: string
  plannedDate: string
  actualDate?: string
  status: MilestoneStatus
  progressPercentage: number
  dependencies: string[]
  deliverables: string[]
}

export interface CreateProjectMemberDto {
  projectId: string
  userId: string
  role: string
  responsibilities: string[]
  canEdit: boolean
  canDelete: boolean
  canManageMembers: boolean
}

export interface UpdateProjectMemberDto {
  role: string
  responsibilities: string[]
  canEdit: boolean
  canDelete: boolean
  canManageMembers: boolean
  isActive: boolean
}
