import {
  Button,
  Card,
  Form,
  Input,
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

interface SafetyTraceRecord {
  id: string
  name: string
  entityId: string
  entityType: string
  asilLevel: string
  status: string
  title: string
  description: string
  requirementId: string
  safetyGoalId: string
}

interface CreateSafetyTraceInput {
  name: string
  description: string
  requirementId: string
  safetyGoalId: string
  hazardAnalysisId: string
  asilLevel: string
  detectionLogicId: string
  projectId: string
  relatedDocuments: string[]
}

const KEY = ['safety-trace-records']
const BASE = '/app/safety-trace-records'

const ASIL_OPTIONS = [
  { value: 'QM', label: 'QM' },
  { value: 'ASIL_A', label: 'ASIL A' },
  { value: 'ASIL_B', label: 'ASIL B' },
  { value: 'ASIL_C', label: 'ASIL C' },
  { value: 'ASIL_D', label: 'ASIL D' },
]

const STATUS_COLOR: Record<string, string> = {
  DRAFT: 'default',
  SUBMITTED: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
}

function useSafetyRecords() {
  return useQuery({ queryKey: KEY, queryFn: () => apiFetch<SafetyTraceRecord[]>(BASE) })
}

function useCreateSafetyRecord() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateSafetyTraceInput) =>
      apiFetch<SafetyTraceRecord>(BASE, { method: 'POST', body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useUpdateSafetyRecord() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: CreateSafetyTraceInput }) =>
      apiFetch<SafetyTraceRecord>(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useDeleteSafetyRecord() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => apiFetch<void>(`${BASE}/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useSubmitSafetyRecord() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => apiFetch<SafetyTraceRecord>(`${BASE}/${id}/submit`, { method: 'POST' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

const defaultValues: CreateSafetyTraceInput = {
  name: '', description: '', requirementId: '', safetyGoalId: '',
  hazardAnalysisId: '', asilLevel: 'QM', detectionLogicId: '', projectId: '',
  relatedDocuments: [],
}

export default function SafetyPage() {
  const { data = [], isLoading } = useSafetyRecords()
  const createMutation = useCreateSafetyRecord()
  const updateMutation = useUpdateSafetyRecord()
  const deleteMutation = useDeleteSafetyRecord()
  const submitMutation = useSubmitSafetyRecord()

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<SafetyTraceRecord | null>(null)
  const [form] = Form.useForm<CreateSafetyTraceInput>()
  const [filter, setFilter] = useState('')

  const displayed = filter
    ? data.filter(r => r.name?.toLowerCase().includes(filter.toLowerCase()) ||
        r.asilLevel?.includes(filter.toUpperCase()))
    : data

  function openCreate() {
    setEditing(null)
    form.setFieldsValue(defaultValues)
    setModalOpen(true)
  }

  function openEdit(row: SafetyTraceRecord) {
    setEditing(row)
    form.setFieldsValue({
      name: row.name ?? row.title,
      description: row.description,
      requirementId: row.requirementId,
      safetyGoalId: row.safetyGoalId,
      hazardAnalysisId: '',
      asilLevel: row.asilLevel,
      detectionLogicId: '',
      projectId: '',
      relatedDocuments: [],
    })
    setModalOpen(true)
  }

  async function handleOk() {
    const values = await form.validateFields()
    if (editing) {
      await updateMutation.mutateAsync({ id: editing.id, input: values })
    } else {
      await createMutation.mutateAsync(values)
    }
    setModalOpen(false)
  }

  const columns: ColumnsType<SafetyTraceRecord> = [
    { title: '名称', dataIndex: 'name', render: (v, r) => v ?? r.title },
    { title: 'ASIL', dataIndex: 'asilLevel', width: 90,
      render: v => <Tag color="orange">{v}</Tag> },
    { title: 'ステータス', dataIndex: 'status', width: 120,
      render: v => <Tag color={STATUS_COLOR[v] ?? 'default'}>{v}</Tag> },
    { title: 'エンティティタイプ', dataIndex: 'entityType', width: 150 },
    {
      title: '操作', key: 'actions', width: 220,
      render: (_: unknown, row) => (
        <Space>
          <Button size="small" onClick={() => openEdit(row)}>編集</Button>
          {row.status === 'DRAFT' && (
            <Button size="small" type="default"
              loading={submitMutation.isPending}
              onClick={() => submitMutation.mutate(row.id)}>
              提出
            </Button>
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
      <Typography.Title level={2} style={{ margin: 0 }}>Safety (ISO 26262)</Typography.Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input.Search placeholder="名称 / ASIL 検索" allowClear style={{ width: 280 }}
            onSearch={setFilter} onChange={e => !e.target.value && setFilter('')} />
          <Button type="primary" onClick={openCreate}>新規作成</Button>
        </Space>
        <Table<SafetyTraceRecord> rowKey="id" columns={columns} dataSource={displayed}
          loading={isLoading} pagination={{ pageSize: 20 }} scroll={{ x: 800 }} />
      </Card>

      <Modal open={modalOpen} title={editing ? '安全記録 編集' : '安全記録 新規作成'}
        onCancel={() => setModalOpen(false)} onOk={handleOk}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        okText={editing ? '更新' : '作成'} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="説明">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="asilLevel" label="ASIL レベル" rules={[{ required: true }]}>
            <Select options={ASIL_OPTIONS} />
          </Form.Item>
          <Form.Item name="requirementId" label="要求仕様 ID">
            <Input placeholder="REQ-xxxx" />
          </Form.Item>
          <Form.Item name="safetyGoalId" label="安全目標 ID">
            <Input placeholder="SG-xxxx" />
          </Form.Item>
          <Form.Item name="hazardAnalysisId" label="ハザード解析 ID">
            <Input placeholder="HARA-xxxx" />
          </Form.Item>
          <Form.Item name="detectionLogicId" label="検出ロジック ID">
            <Input placeholder="UUID (省略可)" />
          </Form.Item>
          <Form.Item name="projectId" label="プロジェクト ID">
            <Input placeholder="UUID (省略可)" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
