import { Layout, Menu, Typography } from 'antd'
import { Link, Outlet, useLocation } from 'react-router-dom'

const { Header, Sider, Content, Footer } = Layout

const items = [
  { key: '/', label: <Link to="/">Home</Link> },
  { key: '/projects', label: <Link to="/projects">Projects</Link> },
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
        <Typography.Text style={{ color: 'rgba(255,255,255,0.65)' }}>
          Java port UI
        </Typography.Text>
      </Header>

      <Layout>
        <Sider width={220} theme="dark">
          <Menu
            mode="inline"
            theme="dark"
            selectedKeys={[location.pathname === '/' ? '/' : location.pathname]}
            items={items}
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
