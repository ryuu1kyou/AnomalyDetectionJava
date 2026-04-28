import { AuthProvider as OidcAuthProvider } from 'react-oidc-context'
import type { WebStorageStateStore } from 'oidc-client-ts'
import type { ReactNode } from 'react'

const oidcConfig = {
  authority: 'http://localhost:44397',
  client_id: 'anomaly-detection-spa',
  redirect_uri: `${window.location.origin}/callback`,
  post_logout_redirect_uri: window.location.origin,
  silent_redirect_uri: `${window.location.origin}/silent-renew.html`,
  scope: 'openid profile email',
  automaticSilentRenew: true,
  userStore: undefined as WebStorageStateStore | undefined,
}

export function AuthProvider({ children }: { children: ReactNode }) {
  return <OidcAuthProvider {...oidcConfig}>{children}</OidcAuthProvider>
}
