import { Card, Space, Table, Tag, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface OemApproval {
  id: string
  entityId: string
  entityType: string
  oemCode: string
  type: string
  status: string
  priority: number
}

export default function OemTraceabilityPage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['oem-approvals'],
    queryFn: () => apiFetch<OemApproval[]>('/app/oem-approvals'),
  })

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>OEM トレーサビリティ</Typography.Title>
      <Card>
        <Table
          rowKey="id"
          loading={isLoading}
          dataSource={data}
          columns={[
            { title: 'エンティティ', dataIndex: 'entityId' },
            { title: 'タイプ', dataIndex: 'entityType', width: 140 },
            { title: 'OEM', dataIndex: 'oemCode', width: 100 },
            {
              title: '承認タイプ',
              dataIndex: 'type',
              width: 120,
              render: (v: string) => <Tag>{v}</Tag>,
            },
            {
              title: 'ステータス',
              dataIndex: 'status',
              width: 120,
              render: (v: string) => (
                <Tag color={v === 'Approved' ? 'success' : v === 'Rejected' ? 'error' : 'default'}>
                  {v}
                </Tag>
              ),
            },
            { title: '優先度', dataIndex: 'priority', width: 80, align: 'right' },
          ]}
          pagination={{ pageSize: 20 }}
        />
      </Card>
    </Space>
  )
}
