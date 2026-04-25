import type { Project, ProjectsQuery } from '../models/project'
import { mockProjects } from '../data/mockProjects'

function includesIgnoreCase(haystack: string, needle: string): boolean {
  return haystack.toLowerCase().includes(needle.trim().toLowerCase())
}

export const projectsApi = {
  async list(query: ProjectsQuery = {}): Promise<Project[]> {
    const filter = (query.filter ?? '').trim()

    return mockProjects
      .filter(p => {
        if (filter) {
          const hay = `${p.projectCode} ${p.projectName}`
          if (!includesIgnoreCase(hay, filter)) return false
        }

        if (query.status !== undefined && query.status !== null) {
          if (p.status !== query.status) return false
        }
        if (query.priority !== undefined && query.priority !== null) {
          if (p.priority !== query.priority) return false
        }
        if (query.vehicleModel) {
          if (!includesIgnoreCase(p.vehicleModel, query.vehicleModel)) return false
        }
        if (query.primarySystem) {
          if (!includesIgnoreCase(p.primarySystem, query.primarySystem)) return false
        }
        return true
      })
      .slice()
      .sort((a, b) => a.projectCode.localeCompare(b.projectCode))
  },

  async getById(projectId: string): Promise<Project | undefined> {
    return mockProjects.find(p => p.id === projectId)
  },
}
