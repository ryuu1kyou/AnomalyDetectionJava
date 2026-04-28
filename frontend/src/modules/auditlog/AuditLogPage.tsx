import { Card, Space, Table, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useQuery } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface AuditLog {
  id: string
  userId: string
  userName: string
  httpMethod: string
  url: string
  actionName: string
  httpStatusCode: number
  executionDuration: number
  occurredAt: string
  exceptions: string | null
}

const METHOD_COLOR: Record<string, string> = {
  GET: 'blue', POST: 'green', PUT: 'orange', DELETE: 'red', PATCH: 'cyan',
}

const STATUS_COLOR = (code: number) => {
  if (code < 300) return 'success'
  if (code < 400) return 'warning'
  return 'error'
}

function useAuditLogs(limit = 100) {
  return useQuery({
    queryKey: ['audit-logs', limit],
    queryFn: () => apiFetch<AuditLog[]>(`/app/audit-logs?limit=${limit}`),
    refetchInterval: 30_000,
  })
}

const columns: ColumnsType<AuditLog> = [
  {
    title: '日時', dataIndex: 'occurredAt', width: 170,
    render: v => new Date(v).toLocaleString('ja-JP'),
    sorter: (a, b) => a.occurredAt.localeCompare(b.occurredAt),
    defaultSortOrder: 'descend',
  },
  {
    title: 'メソッド', dataIndex: 'httpMethod', width: 90,
    render: v => <Tag color={METHOD_COLOR[v] ?? 'default'}>{v}</Tag>,
  },
  {
    title: 'URL', dataIndex: 'url', ellipsis: true,
    render: v => <Typography.Text code style={{ fontSize: 12 }}>{v}</Typography.Text>,
  },
  {
    title: 'ステータス', dataIndex: 'httpStatusCode', width: 90, align: 'right',
    render: v => v ? <Tag color={STATUS_COLOR(v)}>{v}</Tag> : '-',
  },
  {
    title: '実行時間 (ms)', dataIndex: 'executionDuration', width: 120, align: 'right',
    render: v => v != null ? `${v} ms` : '-',
  },
  {
    title: 'ユーザー', dataIndex: 'userName', width: 140,
    render: v => v ?? '-',
  },
  {
    title: 'エラー', dataIndex: 'exceptions', ellipsis: true, width: 200,
    render: v => v ? <Typography.Text type="danger" style={{ fontSize: 12 }}>{v}</Typography.Text> : null,
  },
]

export default function AuditLogPage() {
  const { data = [], isLoading } = useAuditLogs(200)

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>監査ログ</Typography.Title>
      <Card>
        <Table<AuditLog>
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={isLoading}
          pagination={{ pageSize: 50 }}
          scroll={{ x: 1000 }}
          size="small"
        />
      </Card>
    </Space>
  )
}
