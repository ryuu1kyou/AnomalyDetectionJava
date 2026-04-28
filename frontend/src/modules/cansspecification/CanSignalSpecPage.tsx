import { Card, Space, Table, Tag, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface CanSignalSpec {
  id: string
  name: string
  version: string
  oemCode: string
  isActive: boolean
}

export default function CanSignalSpecPage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['cansspecification'],
    queryFn: () => apiFetch<CanSignalSpec[]>('/app/can-signal-specifications'),
  })

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>CAN 信号仕様</Typography.Title>
      <Card>
        <Table
          rowKey="id"
          loading={isLoading}
          dataSource={data}
          columns={[
            { title: '名称', dataIndex: 'name' },
            { title: 'バージョン', dataIndex: 'version', width: 100 },
            { title: 'OEM', dataIndex: 'oemCode', width: 120 },
            {
              title: '有効',
              dataIndex: 'isActive',
              width: 80,
              render: (v: boolean) => <Tag color={v ? 'success' : 'default'}>{v ? 'Yes' : 'No'}</Tag>,
            },
          ]}
          pagination={{ pageSize: 20 }}
        />
      </Card>
    </Space>
  )
}
