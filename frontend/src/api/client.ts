import createClient from 'openapi-fetch'
import { useAuth } from 'react-oidc-context'
import type { paths } from './schema'

export const apiClient = createClient<paths>({ baseUrl: '/api' })

export function useApiClient() {
  const auth = useAuth()

  return createClient<paths>({
    baseUrl: '/api',
    headers: auth.user?.access_token
      ? { Authorization: `Bearer ${auth.user.access_token}` }
      : undefined,
  })
}
