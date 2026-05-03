import { Layout, Menu, Typography } from 'antd'
import type { MenuProps } from 'antd'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { usePermissions } from '../../shared/auth/usePermissions'
import {
  AdminPermissions,
  AnomalyDetectionPermissions,
  CanSignalPermissions,
  DetectionTemplatePermissions,
  IntegrationPermissions,
  KnowledgeBasePermissions,
  OemTraceabilityPermissions,
  ProjectPermissions,
  SafetyTracePermissions,
  SimilarPatternSearchPermissions,
} from '../../shared/auth/permissions'
import type { ReactNode } from 'react'

const { Header, Sider, Content, Footer } = Layout

interface MenuDef {
  key: string
  label: ReactNode
  permission?: string
  children?: MenuDef[]
}

const ALL_MENU_DEFS: MenuDef[] = [
  { key: '/', label: <Link to="/">ホーム</Link> },
  {
    key: 'core',
    label: 'コアドメイン',
    children: [
      { key: '/can-signals', label: <Link to="/can-signals">CAN 信号</Link>, permission: CanSignalPermissions.DEFAULT },
      { key: '/can-signal-specs', label: <Link to="/can-signal-specs">CAN 信号仕様</Link>, permission: CanSignalPermissions.DEFAULT },
      { key: '/detection-templates', label: <Link to="/detection-templates">検出テンプレート</Link>, permission: DetectionTemplatePermissions.DEFAULT },
      { key: '/anomaly-detection', label: <Link to="/anomaly-detection">異常検出ロジック</Link>, permission: AnomalyDetectionPermissions.LOGIC_DEFAULT },
    ],
  },
  {
    key: 'projects',
    label: 'プロジェクト',
    children: [
      { key: '/projects', label: <Link to="/projects">概要</Link>, permission: ProjectPermissions.DEFAULT },
      { key: '/projects/list', label: <Link to="/projects/list">一覧</Link>, permission: ProjectPermissions.DEFAULT },
    ],
  },
  {
    key: 'quality',
    label: '品質・安全',
    children: [
      { key: '/safety', label: <Link to="/safety">Safety (ISO 26262)</Link>, permission: SafetyTracePermissions.DEFAULT },
      { key: '/knowledge-base', label: <Link to="/knowledge-base">ナレッジベース</Link>, permission: KnowledgeBasePermissions.DEFAULT },
      { key: '/oem-traceability', label: <Link to="/oem-traceability">OEM トレーサビリティ</Link>, permission: OemTraceabilityPermissions.APPROVAL_DEFAULT },
      { key: '/similar-pattern-search', label: <Link to="/similar-pattern-search">類似パターン検索</Link>, permission: SimilarPatternSearchPermissions.DEFAULT },
    ],
  },
  {
    key: 'integration',
    label: 'インテグレーション',
    children: [
      { key: '/integration', label: <Link to="/integration">エンドポイント</Link>, permission: IntegrationPermissions.DEFAULT },
    ],
  },
  {
    key: 'admin',
    label: '管理',
    children: [
      { key: '/audit-log', label: <Link to="/audit-log">監査ログ</Link>, permission: AdminPermissions.AUDIT_LOG },
      { key: '/settings', label: <Link to="/settings">設定</Link>, permission: AdminPermissions.SETTINGS },
      { key: '/features', label: <Link to="/features">フィーチャーフラグ</Link>, permission: AdminPermissions.FEATURES },
      { key: '/permissions', label: <Link to="/permissions">権限管理</Link>, permission: AdminPermissions.PERMISSIONS },
    ],
  },
]

function filterMenu(
  defs: MenuDef[],
  hasPermission: (p: string) => boolean,
): MenuProps['items'] {
  const result: NonNullable<MenuProps['items']> = []
  for (const def of defs) {
    if (def.permission && !hasPermission(def.permission)) continue
    if (def.children) {
      const filtered = filterMenu(def.children, hasPermission)
      if (filtered && filtered.length > 0) {
        result.push({ key: def.key, label: def.label, children: filtered })
      }
    } else {
      result.push({ key: def.key, label: def.label })
    }
  }
  return result
}

export default function RootLayout() {
  const location = useLocation()
  const { hasPermission } = usePermissions()

  const visibleMenuItems = filterMenu(ALL_MENU_DEFS, hasPermission)

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 12,
          paddingInline: 16,
          background: '#001529',
        }}
      >
        <Typography.Text style={{ color: 'white', fontWeight: 600 }}>
          AnomalyDetection
        </Typography.Text>
      </Header>

      <Layout>
        <Sider width={220} theme="dark" style={{ overflowY: 'auto' }}>
          <Menu
            mode="inline"
            theme="dark"
            selectedKeys={[location.pathname]}
            defaultOpenKeys={['core', 'projects', 'quality', 'integration', 'admin']}
            items={visibleMenuItems}
          />
        </Sider>

        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>

      <Footer style={{ textAlign: 'center', color: 'rgba(0,0,0,0.45)' }}>
        AnomalyDetection © {new Date().getFullYear()}
      </Footer>
    </Layout>
  )
}
