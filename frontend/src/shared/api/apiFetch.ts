import { User } from 'oidc-client-ts'

function getAccessToken(): string | null {
  // oidc-client-ts stores user in sessionStorage by default
  // Key format: oidc.user:<authority>:<client_id>
  const key = `oidc.user:http://localhost:44397:anomaly-detection-spa`
  try {
    const raw = sessionStorage.getItem(key)
    if (!raw) return null
    const user = JSON.parse(raw) as User
    return user.access_token ?? null
  } catch {
    return null
  }
}

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getAccessToken()
  const res = await fetch(`/api${path}`, {
    ...options,
    headers: {
      'content-type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {}),
    },
  })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(`HTTP ${res.status}: ${text}`)
  }
  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}
