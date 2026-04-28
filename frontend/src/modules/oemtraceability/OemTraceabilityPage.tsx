import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface OemApproval {
  id: string
  entityId: string
  entityType: string
  oemCode: string
  type: string
  status: string
  priority: number
  approvalReason: string
}

interface CreateOemApprovalInput {
  entityId: string
  entityType: string
  oemCode: string
  type: string
  approvalReason: string
  dueDate: string
  priority: number
}

const KEY = ['oem-approvals']
const BASE = '/app/oem-traceability/approvals'

const APPROVAL_TYPE_OPTIONS = [
  { value: 'SAFETY_CRITICAL', label: 'Safety Critical' },
  { value: 'PERFORMANCE', label: 'Performance' },
  { value: 'COMPLIANCE', label: 'Compliance' },
  { value: 'CUSTOMIZATION', label: 'Customization' },
]

const STATUS_COLOR: Record<string, string> = {
  PENDING: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
  CANCELLED: 'default',
}

function useApprovals() {
  return useQuery({ queryKey: KEY, queryFn: () => apiFetch<OemApproval[]>(BASE) })
}

function useCreateApproval() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateOemApprovalInput) =>
      apiFetch<OemApproval>(BASE, { method: 'POST', body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useDeleteApproval() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => apiFetch<void>(`${BASE}/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useApprovalAction() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, action }: { id: string; action: 'approve' | 'reject' | 'cancel' }) =>
      apiFetch<OemApproval>(`${BASE}/${id}/${action}`, { method: 'POST' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

const defaultValues: CreateOemApprovalInput = {
  entityId: '', entityType: '', oemCode: '', type: 'SAFETY_CRITICAL',
  approvalReason: '', dueDate: '', priority: 5,
}

export default function OemTraceabilityPage() {
  const { data = [], isLoading } = useApprovals()
  const createMutation = useCreateApproval()
  const deleteMutation = useDeleteApproval()
  const actionMutation = useApprovalAction()

  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm<CreateOemApprovalInput>()
  const [filter, setFilter] = useState('')

  const displayed = filter
    ? data.filter(a => a.oemCode.toLowerCase().includes(filter.toLowerCase()) ||
        a.entityType.toLowerCase().includes(filter.toLowerCase()))
    : data

  function openCreate() {
    form.setFieldsValue(defaultValues)
    setModalOpen(true)
  }

  async function handleOk() {
    const values = await form.validateFields()
    await createMutation.mutateAsync(values)
    setModalOpen(false)
  }

  const columns: ColumnsType<OemApproval> = [
    { title: 'エンティティ', dataIndex: 'entityId', ellipsis: true },
    { title: 'タイプ', dataIndex: 'entityType', width: 130 },
    { title: 'OEM', dataIndex: 'oemCode', width: 100,
      render: v => <Tag color="blue">{v}</Tag> },
    { title: '承認タイプ', dataIndex: 'type', width: 140,
      render: v => <Tag>{v}</Tag> },
    { title: 'ステータス', dataIndex: 'status', width: 110,
      render: v => <Tag color={STATUS_COLOR[v] ?? 'default'}>{v}</Tag> },
    { title: '優先度', dataIndex: 'priority', width: 70, align: 'right' },
    {
      title: '操作', key: 'actions', width: 260,
      render: (_: unknown, row) => (
        <Space>
          {row.status === 'PENDING' && (
            <>
              <Button size="small" type="primary"
                loading={actionMutation.isPending}
                onClick={() => actionMutation.mutate({ id: row.id, action: 'approve' })}>
                承認
              </Button>
              <Button size="small" danger
                loading={actionMutation.isPending}
                onClick={() => actionMutation.mutate({ id: row.id, action: 'reject' })}>
                却下
              </Button>
            </>
          )}
          <Popconfirm title="削除しますか？" onConfirm={() => deleteMutation.mutate(row.id)}
            okText="削除" cancelText="キャンセル">
            <Button size="small" danger>削除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>OEM トレーサビリティ</Typography.Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input.Search placeholder="OEM / エンティティタイプ検索" allowClear style={{ width: 280 }}
            onSearch={setFilter} onChange={e => !e.target.value && setFilter('')} />
          <Button type="primary" onClick={openCreate}>新規承認申請</Button>
        </Space>
        <Table<OemApproval> rowKey="id" columns={columns} dataSource={displayed}
          loading={isLoading} pagination={{ pageSize: 20 }} scroll={{ x: 900 }} />
      </Card>

      <Modal open={modalOpen} title="OEM 承認申請 新規作成"
        onCancel={() => setModalOpen(false)} onOk={handleOk}
        confirmLoading={createMutation.isPending}
        okText="作成" destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="entityId" label="エンティティ ID" rules={[{ required: true }]}>
            <Input placeholder="UUID" />
          </Form.Item>
          <Form.Item name="entityType" label="エンティティタイプ" rules={[{ required: true }]}>
            <Input placeholder="例: DetectionLogic" />
          </Form.Item>
          <Form.Item name="oemCode" label="OEM コード" rules={[{ required: true }]}>
            <Input placeholder="例: OEM-A" />
          </Form.Item>
          <Form.Item name="type" label="承認タイプ" rules={[{ required: true }]}>
            <Select options={APPROVAL_TYPE_OPTIONS} />
          </Form.Item>
          <Form.Item name="approvalReason" label="承認理由">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="dueDate" label="期限">
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item name="priority" label="優先度 (1–10)">
            <InputNumber style={{ width: '100%' }} min={1} max={10} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
