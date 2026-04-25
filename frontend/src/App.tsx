import { Button, ConfigProvider, Space, Typography } from 'antd'
import './App.css'

export default function App() {
  return (
    <ConfigProvider>
      <div style={{ padding: 24 }}>
        <Space direction="vertical" size="middle">
          <Typography.Title level={2} style={{ margin: 0 }}>
            AnomalyDetection (Frontend)
          </Typography.Title>
          <Typography.Text type="secondary">
            Vite + React + TypeScript + Ant Design
          </Typography.Text>
          <Space>
            <Button type="primary">Primary</Button>
            <Button>Default</Button>
          </Space>
        </Space>
      </div>
    </ConfigProvider>
  )
}
