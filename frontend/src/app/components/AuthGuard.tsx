import { Spin } from 'antd'
import { useAuth } from 'react-oidc-context'
import type { ReactNode } from 'react'

export default function AuthGuard({ children }: { children: ReactNode }) {
  const auth = useAuth()

  if (auth.isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="認証中..." />
      </div>
    )
  }

  if (auth.error) {
    return (
      <div style={{ padding: 24 }}>
        認証エラー: {auth.error.message}
      </div>
    )
  }

  if (!auth.isAuthenticated) {
    auth.signinRedirect()
    return null
  }

  return <>{children}</>
}
