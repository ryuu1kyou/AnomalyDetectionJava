import { Layout, Menu, Typography } from 'antd'
import { Link, Outlet, useLocation } from 'react-router-dom'

const { Header, Sider, Content, Footer } = Layout

const MENU_ITEMS = [
  { key: '/', label: <Link to="/">ホーム</Link> },
  {
    key: 'core',
    label: 'コアドメイン',
    children: [
      { key: '/can-signals', label: <Link to="/can-signals">CAN 信号</Link> },
      { key: '/can-signal-specs', label: <Link to="/can-signal-specs">CAN 信号仕様</Link> },
      { key: '/detection-templates', label: <Link to="/detection-templates">検出テンプレート</Link> },
      { key: '/anomaly-detection', label: <Link to="/anomaly-detection">異常検出ロジック</Link> },
    ],
  },
  {
    key: 'projects',
    label: 'プロジェクト',
    children: [
      { key: '/projects', label: <Link to="/projects">概要</Link> },
      { key: '/projects/list', label: <Link to="/projects/list">一覧</Link> },
    ],
  },
  {
    key: 'quality',
    label: '品質・安全',
    children: [
      { key: '/safety', label: <Link to="/safety">Safety (ISO 26262)</Link> },
      { key: '/knowledge-base', label: <Link to="/knowledge-base">ナレッジベース</Link> },
      { key: '/oem-traceability', label: <Link to="/oem-traceability">OEM トレーサビリティ</Link> },
      { key: '/similar-pattern-search', label: <Link to="/similar-pattern-search">類似パターン検索</Link> },
    ],
  },
  {
    key: 'integration',
    label: 'インテグレーション',
    children: [
      { key: '/integration', label: <Link to="/integration">エンドポイント</Link> },
    ],
  },
  {
    key: 'admin',
    label: '管理',
    children: [
      { key: '/audit-log', label: <Link to="/audit-log">監査ログ</Link> },
      { key: '/settings', label: <Link to="/settings">設定</Link> },
      { key: '/features', label: <Link to="/features">フィーチャーフラグ</Link> },
      { key: '/permissions', label: <Link to="/permissions">権限管理</Link> },
    ],
  },
]

export default function RootLayout() {
  const location = useLocation()

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
            items={MENU_ITEMS}
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
