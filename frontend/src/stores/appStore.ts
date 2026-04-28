import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AppState {
  locale: string
  tenantId: string | null
  sidebarCollapsed: boolean
  setLocale: (locale: string) => void
  setTenantId: (tenantId: string | null) => void
  setSidebarCollapsed: (collapsed: boolean) => void
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      locale: 'ja',
      tenantId: null,
      sidebarCollapsed: false,
      setLocale: (locale) => set({ locale }),
      setTenantId: (tenantId) => set({ tenantId }),
      setSidebarCollapsed: (sidebarCollapsed) => set({ sidebarCollapsed }),
    }),
    { name: 'app-store' },
  ),
)
