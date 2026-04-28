import { create } from 'zustand'

interface AuthUser {
  sub: string
  name?: string
  email?: string
  roles?: string[]
  tenantId?: string
}

interface AuthState {
  user: AuthUser | null
  setUser: (user: AuthUser | null) => void
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
}))
