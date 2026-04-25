import { Card, Space, Typography } from 'antd'
import { Link } from 'react-router-dom'

export default function ProjectsPage() {
  return (
    <Space direction="vertical" size="middle" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>
        Projects
      </Typography.Title>

      <Card>
        <Typography.Paragraph style={{ margin: 0 }}>
          移植元(.NET)の Project 画面/機能に対応する UI をここに実装していきます。
        </Typography.Paragraph>
        <Typography.Paragraph style={{ marginBottom: 0 }}>
          <Link to="/projects/list">プロジェクト一覧へ</Link>
        </Typography.Paragraph>
      </Card>
    </Space>
  )
}
