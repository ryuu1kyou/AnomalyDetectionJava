import { Card, Space, Switch, Table, Typography, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface FeatureFlag {
  name: string
  enabled: boolean
  scope: string
  scopeId: string | null
}

const KEY = ['features']
const BASE = '/app/features'

function useFeatures() {
  return useQuery({ queryKey: KEY, queryFn: () => apiFetch<FeatureFlag[]>(BASE) })
}

function useSetFeature() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ name, enabled }: { name: string; enabled: boolean }) =>
      apiFetch<void>(`${BASE}/${encodeURIComponent(name)}`, {
        method: 'PUT', body: JSON.stringify({ enabled }),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export default function FeaturesPage() {
  const { data = [], isLoading } = useFeatures()
  const setMutation = useSetFeature()

  const columns: ColumnsType<FeatureFlag> = [
    {
      title: 'フィーチャー名', dataIndex: 'name',
      render: v => <Typography.Text strong>{v}</Typography.Text>,
    },
    { title: 'スコープ', dataIndex: 'scope', width: 100 },
    { title: 'スコープ ID', dataIndex: 'scopeId', width: 160, render: v => v ?? 'グローバル' },
    {
      title: '有効', dataIndex: 'enabled', width: 100,
      render: (v: boolean, row) => (
        <Switch
          checked={v}
          loading={setMutation.isPending}
          onChange={checked => {
            setMutation.mutate({ name: row.name, enabled: checked })
            message.info(`${row.name}: ${checked ? '有効化' : '無効化'}しました`)
          }}
        />
      ),
    },
  ]

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>フィーチャーフラグ管理</Typography.Title>
      <Card>
        <Table<FeatureFlag>
          rowKey={r => `${r.name}:${r.scope}:${r.scopeId ?? ''}`}
          columns={columns}
          dataSource={data}
          loading={isLoading}
          pagination={{ pageSize: 20 }}
        />
      </Card>
    </Space>
  )
}
