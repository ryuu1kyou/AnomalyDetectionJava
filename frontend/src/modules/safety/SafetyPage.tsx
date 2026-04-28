import { Card, Space, Table, Tag, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface SafetyTraceRecord {
  id: string
  entityId: string
  entityType: string
  asilLevel: string
  status: string
  title: string
}

export default function SafetyPage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['safety-trace'],
    queryFn: () => apiFetch<SafetyTraceRecord[]>('/app/safety-trace'),
  })

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>Safety (ISO 26262)</Typography.Title>
      <Card>
        <Table
          rowKey="id"
          loading={isLoading}
          dataSource={data}
          columns={[
            { title: 'タイトル', dataIndex: 'title' },
            { title: 'エンティティタイプ', dataIndex: 'entityType', width: 160 },
            {
              title: 'ASIL',
              dataIndex: 'asilLevel',
              width: 90,
              render: (v: string) => <Tag color="orange">{v}</Tag>,
            },
            { title: 'ステータス', dataIndex: 'status', width: 120 },
          ]}
          pagination={{ pageSize: 20 }}
        />
      </Card>
    </Space>
  )
}
