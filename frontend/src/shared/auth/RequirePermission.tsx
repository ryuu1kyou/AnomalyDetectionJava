import type { ReactNode } from 'react'
import { usePermissions } from './usePermissions'

interface RequirePermissionProps {
  permission: string
  fallback?: ReactNode
  children: ReactNode
}

/** Conditionally renders children only when the user holds the given permission.
 * Renders `fallback` (default: nothing) when permission is absent. */
export function RequirePermission({
  permission,
  fallback = null,
  children,
}: RequirePermissionProps) {
  const { hasPermission } = usePermissions()
  if (!hasPermission(permission)) return <>{fallback}</>
  return <>{children}</>
}
