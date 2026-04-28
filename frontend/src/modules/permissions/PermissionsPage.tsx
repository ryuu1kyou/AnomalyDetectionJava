import {
  Button,
  Card,
  Col,
  Form,
  Input,
  Popconfirm,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface PermissionGroup {
  name: string
  displayName: string
  permissionNames: string[]
}

interface GrantDto {
  name: string
  providerName: string
  providerKey: string
}

const GRANTS_KEY = ['permission-grants']
const DEFS_KEY = ['permission-definitions']
const BASE = '/app/permissions'

function useDefinitions() {
  return useQuery({ queryKey: DEFS_KEY, queryFn: () => apiFetch<PermissionGroup[]>(`${BASE}/definitions`) })
}

function useGrants() {
  return useQuery({ queryKey: GRANTS_KEY, queryFn: () => apiFetch<GrantDto[]>(`${BASE}/grants`) })
}

function useGrantToRole() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ role, permission }: { role: string; permission: string }) =>
      apiFetch<void>(`${BASE}/roles/${encodeURIComponent(role)}/grant`, {
        method: 'POST', body: JSON.stringify({ permission }),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: GRANTS_KEY }),
  })
}

function useRevokeFromRole() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ role, permission }: { role: string; permission: string }) =>
      apiFetch<void>(`${BASE}/roles/${encodeURIComponent(role)}/grants/${encodeURIComponent(permission)}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: GRANTS_KEY }),
  })
}

export default function PermissionsPage() {
  const { data: groups = [], isLoading: defsLoading } = useDefinitions()
  const { data: grants = [], isLoading: grantsLoading } = useGrants()
  const grantMutation = useGrantToRole()
  const revokeMutation = useRevokeFromRole()
  const [form] = Form.useForm<{ role: string; permission: string }>()

  const allPermissions = groups.flatMap(g => g.permissionNames).map(n => ({ value: n, label: n }))

  async function handleGrant() {
    const { role, permission } = await form.validateFields()
    await grantMutation.mutateAsync({ role, permission })
    message.success('権限を付与しました')
    form.resetFields()
  }

  const grantColumns: ColumnsType<GrantDto> = [
    {
      title: '権限名', dataIndex: 'name',
      render: v => <Typography.Text code style={{ fontSize: 12 }}>{v}</Typography.Text>,
    },
    {
      title: 'プロバイダ', dataIndex: 'providerName', width: 90,
      render: v => <Tag>{v === 'R' ? 'ロール' : 'ユーザー'}</Tag>,
    },
    { title: 'ロール / ユーザー', dataIndex: 'providerKey', width: 160 },
    {
      title: '操作', key: 'actions', width: 80,
      render: (_: unknown, row) => (
        <Popconfirm
          title="権限を剥奪しますか？"
          onConfirm={() => revokeMutation.mutate({ role: row.providerKey, permission: row.name })}
          okText="剥奪" cancelText="キャンセル">
          <Button size="small" danger loading={revokeMutation.isPending}>削除</Button>
        </Popconfirm>
      ),
    },
  ]

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>権限管理</Typography.Title>

      <Row gutter={16}>
        <Col span={10}>
          <Card title="定義済み権限グループ" loading={defsLoading}>
            {groups.map(g => (
              <div key={g.name} style={{ marginBottom: 12 }}>
                <Typography.Text strong>{g.displayName}</Typography.Text>
                <div style={{ marginTop: 4, display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                  {g.permissionNames.map(p => <Tag key={p} style={{ fontSize: 11 }}>{p}</Tag>)}
                </div>
              </div>
            ))}
          </Card>
        </Col>

        <Col span={14}>
          <Card title="ロールへの権限付与" style={{ marginBottom: 16 }}>
            <Form form={form} layout="inline" onFinish={handleGrant}>
              <Form.Item name="role" rules={[{ required: true }]}>
                <Input placeholder="ロール名 (例: admin)" style={{ width: 160 }} />
              </Form.Item>
              <Form.Item name="permission" rules={[{ required: true }]}>
                <Select options={allPermissions} placeholder="権限を選択" style={{ width: 240 }} showSearch />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" loading={grantMutation.isPending}>付与</Button>
              </Form.Item>
            </Form>
          </Card>

          <Card title="付与済み権限一覧">
            <Table<GrantDto>
              rowKey={r => `${r.name}:${r.providerKey}`}
              columns={grantColumns}
              dataSource={grants}
              loading={grantsLoading}
              pagination={{ pageSize: 15 }}
              size="small"
            />
          </Card>
        </Col>
      </Row>
    </Space>
  )
}
