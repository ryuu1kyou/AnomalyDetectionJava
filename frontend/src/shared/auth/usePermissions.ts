import { useAuth } from 'react-oidc-context'
import { useMemo, useCallback } from 'react'

/** Parse a JWT access-token payload without external libraries. */
function parseJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1]
    // Base64URL -> Base64
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, '=')
    const json = atob(padded)
    return JSON.parse(json)
  } catch {
    return null
  }
}

export function usePermissions() {
  const auth = useAuth()

  const permissions = useMemo(() => {
    const token = auth.user?.access_token
    if (!token) return [] as string[]
    const payload = parseJwtPayload(token)
    const perms = payload?.permissions
    if (Array.isArray(perms) && perms.every((p) => typeof p === 'string')) {
      return perms as string[]
    }
    return [] as string[]
  }, [auth.user?.access_token])

  const hasPermission = useCallback(
    (name: string) => permissions.includes(name),
    [permissions],
  )

  const hasAnyPermission = useCallback(
    (names: string[]) => names.some((n) => permissions.includes(n)),
    [permissions],
  )

  const hasAllPermissions = useCallback(
    (names: string[]) => names.every((n) => permissions.includes(n)),
    [permissions],
  )

  return {
    permissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    isAuthenticated: !!auth.user,
    isLoading: auth.isLoading,
  }
}
