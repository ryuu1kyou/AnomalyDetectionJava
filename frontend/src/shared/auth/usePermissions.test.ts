import { describe, expect, it, vi, beforeEach } from 'vitest'
import { renderHook } from '@testing-library/react'
import { usePermissions } from './usePermissions'

// Mock react-oidc-context
vi.mock('react-oidc-context', () => ({
  useAuth: vi.fn(),
}))

import { useAuth } from 'react-oidc-context'

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>

function makeToken(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'RS256', typ: 'JWT' }))
  const body = btoa(JSON.stringify(payload))
  return `${header}.${body}.fakesig`
}

describe('usePermissions', () => {
  beforeEach(() => {
    mockUseAuth.mockReturnValue({ user: null, isLoading: false })
  })

  it('returns empty permissions when not authenticated', () => {
    const { result } = renderHook(() => usePermissions())
    expect(result.current.permissions).toEqual([])
    expect(result.current.isAuthenticated).toBe(false)
  })

  it('returns permissions from JWT access_token', () => {
    mockUseAuth.mockReturnValue({
      user: { access_token: makeToken({ permissions: ['CanSignal.Default', 'Projects.Projects.Default'] }) },
      isLoading: false,
    })
    const { result } = renderHook(() => usePermissions())
    expect(result.current.permissions).toEqual(['CanSignal.Default', 'Projects.Projects.Default'])
  })

  it('hasPermission returns true for a held permission', () => {
    mockUseAuth.mockReturnValue({
      user: { access_token: makeToken({ permissions: ['CanSignal.Default'] }) },
      isLoading: false,
    })
    const { result } = renderHook(() => usePermissions())
    expect(result.current.hasPermission('CanSignal.Default')).toBe(true)
    expect(result.current.hasPermission('CanSignal.Create')).toBe(false)
  })

  it('hasAnyPermission returns true when at least one matches', () => {
    mockUseAuth.mockReturnValue({
      user: { access_token: makeToken({ permissions: ['CanSignal.Default'] }) },
      isLoading: false,
    })
    const { result } = renderHook(() => usePermissions())
    expect(result.current.hasAnyPermission(['Missing', 'CanSignal.Default'])).toBe(true)
    expect(result.current.hasAnyPermission(['Missing', 'Other'])).toBe(false)
  })

  it('hasAllPermissions returns true only when all match', () => {
    mockUseAuth.mockReturnValue({
      user: { access_token: makeToken({ permissions: ['A', 'B'] }) },
      isLoading: false,
    })
    const { result } = renderHook(() => usePermissions())
    expect(result.current.hasAllPermissions(['A', 'B'])).toBe(true)
    expect(result.current.hasAllPermissions(['A', 'C'])).toBe(false)
  })

  it('returns empty permissions when JWT has no permissions claim', () => {
    mockUseAuth.mockReturnValue({
      user: { access_token: makeToken({ sub: 'user123' }) },
      isLoading: false,
    })
    const { result } = renderHook(() => usePermissions())
    expect(result.current.permissions).toEqual([])
  })

  it('returns empty permissions when permissions claim is not an array', () => {
    mockUseAuth.mockReturnValue({
      user: { access_token: makeToken({ permissions: 'not-an-array' }) },
      isLoading: false,
    })
    const { result } = renderHook(() => usePermissions())
    expect(result.current.permissions).toEqual([])
  })

  it('returns empty permissions when access_token is malformed', () => {
    mockUseAuth.mockReturnValue({
      user: { access_token: 'not.a.valid.jwt.with.five.parts' },
      isLoading: false,
    })
    const { result } = renderHook(() => usePermissions())
    expect(result.current.permissions).toEqual([])
  })

  it('returns empty permissions when access_token is empty string', () => {
    mockUseAuth.mockReturnValue({
      user: { access_token: '' },
      isLoading: false,
    })
    const { result } = renderHook(() => usePermissions())
    expect(result.current.permissions).toEqual([])
  })
})
