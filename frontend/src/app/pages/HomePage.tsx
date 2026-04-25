import { Card, Space, Typography } from 'antd'

export default function HomePage() {
  return (
    <Space direction="vertical" size="middle" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>
        Home
      </Typography.Title>

      <Card>
        <Typography.Paragraph style={{ margin: 0 }}>
          ここはフロントのスケルトンです。次は画面/ドメイン単位のモジュールを足していきます。
        </Typography.Paragraph>
      </Card>
    </Space>
  )
}
