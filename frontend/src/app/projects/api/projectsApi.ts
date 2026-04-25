import type {
  CreateProjectMemberDto,
  CreateProjectMilestoneDto,
  GetProjectsInput,
  PagedResult,
  Project,
  ProjectMember,
  ProjectMilestone,
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

  // Backward compatible alias (older UI used `list`)
  async list(input: GetProjectsInput = {}): Promise<Project[]> {
    const result = await this.getList(input)
    return result.items
  },

  // ---- Milestones (mock) ----
  async getMilestones(projectId: string): Promise<ProjectMilestone[]> {
    void projectId
    return []
  },
  async createMilestone(input: CreateProjectMilestoneDto): Promise<ProjectMilestone> {
    void input
    throw new Error('Not implemented (mock)')
  },
  async updateMilestone(
    milestoneId: string,
    input: UpdateProjectMilestoneDto
  ): Promise<ProjectMilestone> {
    void milestoneId
    void input
    throw new Error('Not implemented (mock)')
  },
  async deleteMilestone(milestoneId: string): Promise<void> {
    void milestoneId
    throw new Error('Not implemented (mock)')
  },

  // ---- Members (mock) ----
  async getMembers(projectId: string): Promise<ProjectMember[]> {
    void projectId
    return []
  },
  async addMember(input: CreateProjectMemberDto): Promise<ProjectMember> {
    void input
    throw new Error('Not implemented (mock)')
  },
  async updateMember(memberId: string, input: UpdateProjectMemberDto): Promise<ProjectMember> {
    void memberId
    void input
    throw new Error('Not implemented (mock)')
  },
  async removeMember(memberId: string): Promise<void> {
    void memberId
    throw new Error('Not implemented (mock)')
  },
}
