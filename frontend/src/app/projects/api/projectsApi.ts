import type {
  CreateProjectDto,
  CreateProjectMemberDto,
  CreateProjectMilestoneDto,
  GetProjectsInput,
  PagedResult,
  Project,
  ProjectMember,
  ProjectMilestone,
  UpdateProjectDto,
  UpdateProjectMemberDto,
  UpdateProjectMilestoneDto,
} from '../models/project'
import { mockProjects } from '../data/mockProjects'

// Use Vite dev-server proxy for local dev (avoids CORS); production should use same-origin.
const BACKEND_BASE_URL = ''
const API_BASE_PATH = '/api/app/anomaly-detection-project'

async function fetchJson<T>(pathWithQuery: string): Promise<T> {
  const res = await fetch(`${BACKEND_BASE_URL}${pathWithQuery}`)
  if (!res.ok) {
    throw new Error(`HTTP ${res.status} ${res.statusText}`)
  }
  return (await res.json()) as T
}

function toQueryString(params: Record<string, string | number | undefined | null>): string {
  const usp = new URLSearchParams()
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null) continue
    const s = String(v)
    if (s.trim() === '') continue
    usp.set(k, s)
  }
  const qs = usp.toString()
  return qs ? `?${qs}` : ''
}

function includesIgnoreCase(haystack: string, needle: string): boolean {
  return haystack.toLowerCase().includes(needle.trim().toLowerCase())
}

export const projectsApi = {
  async getList(input: GetProjectsInput = {}): Promise<PagedResult<Project>> {
    // Backend first (if running). Fallback to mock.
    try {
      const qs = toQueryString({
        filter: input.filter ?? undefined,
        status: input.status ?? undefined,
        priority: input.priority ?? undefined,
        vehicleModel: input.vehicleModel ?? undefined,
        primarySystem: input.primarySystem ?? undefined,
        skipCount: input.skipCount ?? 0,
        maxResultCount: input.maxResultCount ?? 10,
        sorting: input.sorting ?? 'projectCode asc',
      })
      return await fetchJson<PagedResult<Project>>(`${API_BASE_PATH}${qs}`)
    } catch {
      // ignore and fallback
    }

    const filter = (input.filter ?? '').trim()

    const all = mockProjects
      .filter(p => {
        if (filter) {
          const hay = `${p.projectCode} ${p.projectName}`
          if (!includesIgnoreCase(hay, filter)) return false
        }

        if (input.status !== undefined && input.status !== null) {
          if (p.status !== input.status) return false
        }
        if (input.priority !== undefined && input.priority !== null) {
          if (p.priority !== input.priority) return false
        }
        if (input.vehicleModel) {
          if (!includesIgnoreCase(p.vehicleModel, input.vehicleModel)) return false
        }
        if (input.primarySystem) {
          if (!includesIgnoreCase(p.primarySystem, input.primarySystem)) return false
        }
        return true
      })
      .slice()
      .sort((a, b) => a.projectCode.localeCompare(b.projectCode))

    const skip = Math.max(0, input.skipCount ?? 0)
    const take = Math.max(1, input.maxResultCount ?? 10)
    const page = all.slice(skip, skip + take)

    return { items: page, totalCount: all.length }
  },

  async getById(projectId: string): Promise<Project | undefined> {
    try {
      return await fetchJson<Project>(`${API_BASE_PATH}/${encodeURIComponent(projectId)}`)
    } catch {
      // ignore and fallback
    }
    return mockProjects.find(p => p.id === projectId)
  },

  async create(input: CreateProjectDto): Promise<Project> {
    const res = await fetch(`${BACKEND_BASE_URL}${API_BASE_PATH}`, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify(input),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`)
    return (await res.json()) as Project
  },

  async update(projectId: string, input: UpdateProjectDto): Promise<Project> {
    const res = await fetch(
      `${BACKEND_BASE_URL}${API_BASE_PATH}/${encodeURIComponent(projectId)}`,
      {
        method: 'PUT',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(input),
      }
    )
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`)
    return (await res.json()) as Project
  },

  async deleteProject(projectId: string): Promise<void> {
    const res = await fetch(
      `${BACKEND_BASE_URL}${API_BASE_PATH}/${encodeURIComponent(projectId)}`,
      { method: 'DELETE' }
    )
    if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status} ${res.statusText}`)
  },

  // Backward compatible alias (older UI used `list`)
  async list(input: GetProjectsInput = {}): Promise<Project[]> {
    const result = await this.getList(input)
    return result.items
  },

  // ---- Milestones (mock) ----
  async getMilestones(projectId: string): Promise<ProjectMilestone[]> {
    try {
      return await fetchJson<ProjectMilestone[]>(
        `${API_BASE_PATH}/${encodeURIComponent(projectId)}/milestones`
      )
    } catch {
      return []
    }
  },
  async createMilestone(input: CreateProjectMilestoneDto): Promise<ProjectMilestone> {
    const res = await fetch(`${BACKEND_BASE_URL}${API_BASE_PATH}/milestones`, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify(input),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`)
    return (await res.json()) as ProjectMilestone
  },
  async updateMilestone(
    milestoneId: string,
    input: UpdateProjectMilestoneDto
  ): Promise<ProjectMilestone> {
    const res = await fetch(
      `${BACKEND_BASE_URL}${API_BASE_PATH}/milestones/${encodeURIComponent(milestoneId)}`,
      {
        method: 'PUT',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(input),
      }
    )
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`)
    return (await res.json()) as ProjectMilestone
  },
  async deleteMilestone(milestoneId: string): Promise<void> {
    const res = await fetch(
      `${BACKEND_BASE_URL}${API_BASE_PATH}/milestones/${encodeURIComponent(milestoneId)}`,
      { method: 'DELETE' }
    )
    if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status} ${res.statusText}`)
  },

  async completeMilestone(milestoneId: string): Promise<ProjectMilestone> {
    const res = await fetch(
      `${BACKEND_BASE_URL}${API_BASE_PATH}/milestones/${encodeURIComponent(milestoneId)}/complete`,
      { method: 'POST' }
    )
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`)
    return (await res.json()) as ProjectMilestone
  },

  // ---- Members (mock) ----
  async getMembers(projectId: string): Promise<ProjectMember[]> {
    try {
      return await fetchJson<ProjectMember[]>(
        `${API_BASE_PATH}/${encodeURIComponent(projectId)}/members`
      )
    } catch {
      return []
    }
  },
  async addMember(input: CreateProjectMemberDto): Promise<ProjectMember> {
    const res = await fetch(`${BACKEND_BASE_URL}${API_BASE_PATH}/members`, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify(input),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`)
    return (await res.json()) as ProjectMember
  },
  async updateMember(memberId: string, input: UpdateProjectMemberDto): Promise<ProjectMember> {
    const res = await fetch(
      `${BACKEND_BASE_URL}${API_BASE_PATH}/members/${encodeURIComponent(memberId)}`,
      {
        method: 'PUT',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(input),
      }
    )
    if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`)
    return (await res.json()) as ProjectMember
  },
  async removeMember(memberId: string): Promise<void> {
    const res = await fetch(
      `${BACKEND_BASE_URL}${API_BASE_PATH}/members/${encodeURIComponent(memberId)}`,
      { method: 'DELETE' }
    )
    if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status} ${res.statusText}`)
  },
}
