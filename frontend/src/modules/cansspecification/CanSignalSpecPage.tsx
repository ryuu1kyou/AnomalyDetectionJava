import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface CanSignalSpec {
  id: string
  name: string
  signalIdentifier: string
  version: string
  oemCode: string
  isActive: boolean
  unit: string
  minValue: number
  maxValue: number
}

interface CreateUpdateCanSignalSpecInput {
  signalIdentifier: string
  name: string
  systemCategoryId: string
  conversionType: string
  offset: number
  gain: number
  minValue: number
  maxValue: number
  unit: string
  description: string
}

const KEY = ['cansspecification']
const BASE = '/app/can-signal-specifications'

function useSpecs() {
  return useQuery({ queryKey: KEY, queryFn: () => apiFetch<CanSignalSpec[]>(BASE) })
}

function useCreateSpec() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateUpdateCanSignalSpecInput) =>
      apiFetch<CanSignalSpec>(BASE, { method: 'POST', body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useUpdateSpec() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: CreateUpdateCanSignalSpecInput }) =>
      apiFetch<CanSignalSpec>(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useDeleteSpec() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => apiFetch<void>(`${BASE}/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

const defaultValues: CreateUpdateCanSignalSpecInput = {
  signalIdentifier: '', name: '', systemCategoryId: '',
  conversionType: 'LINEAR', offset: 0, gain: 1,
  minValue: 0, maxValue: 100, unit: '', description: '',
}

export default function CanSignalSpecPage() {
  const { data = [], isLoading } = useSpecs()
  const createMutation = useCreateSpec()
  const updateMutation = useUpdateSpec()
  const deleteMutation = useDeleteSpec()

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<CanSignalSpec | null>(null)
  const [form] = Form.useForm<CreateUpdateCanSignalSpecInput>()
  const [filter, setFilter] = useState('')

  const displayed = filter
    ? data.filter(s => s.name.toLowerCase().includes(filter.toLowerCase()) ||
        s.signalIdentifier?.toLowerCase().includes(filter.toLowerCase()))
    : data

  function openCreate() {
    setEditing(null)
    form.setFieldsValue(defaultValues)
    setModalOpen(true)
  }

  function openEdit(row: CanSignalSpec) {
    setEditing(row)
    form.setFieldsValue({
      signalIdentifier: row.signalIdentifier ?? '',
      name: row.name, systemCategoryId: '',
      conversionType: 'LINEAR', offset: 0, gain: 1,
      minValue: row.minValue ?? 0, maxValue: row.maxValue ?? 100,
      unit: row.unit ?? '', description: '',
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

  const columns: ColumnsType<CanSignalSpec> = [
    { title: '識別子', dataIndex: 'signalIdentifier', width: 160 },
    { title: '名称', dataIndex: 'name',
      render: v => <Typography.Text strong>{v}</Typography.Text> },
    { title: '単位', dataIndex: 'unit', width: 80 },
    { title: '最小値', dataIndex: 'minValue', width: 90, align: 'right' },
    { title: '最大値', dataIndex: 'maxValue', width: 90, align: 'right' },
    { title: '有効', dataIndex: 'isActive', width: 80,
      render: (v: boolean) => <Tag color={v ? 'success' : 'default'}>{v ? 'Yes' : 'No'}</Tag> },
    {
      title: '操作', key: 'actions', width: 140,
      render: (_: unknown, row) => (
        <Space>
          <Button size="small" onClick={() => openEdit(row)}>編集</Button>
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
      <Typography.Title level={2} style={{ margin: 0 }}>CAN 信号仕様</Typography.Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input.Search placeholder="名称 / 識別子検索" allowClear style={{ width: 280 }}
            onSearch={setFilter} onChange={e => !e.target.value && setFilter('')} />
          <Button type="primary" onClick={openCreate}>新規作成</Button>
        </Space>
        <Table<CanSignalSpec> rowKey="id" columns={columns} dataSource={displayed}
          loading={isLoading} pagination={{ pageSize: 20 }} scroll={{ x: 800 }} />
      </Card>

      <Modal open={modalOpen} title={editing ? 'CAN 信号仕様 編集' : 'CAN 信号仕様 新規作成'}
        onCancel={() => setModalOpen(false)} onOk={handleOk}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        okText={editing ? '更新' : '作成'} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="signalIdentifier" label="識別子" rules={[{ required: true }]}>
            <Input placeholder="例: ENG_SPEED_01" />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="unit" label="単位">
            <Input placeholder="例: rpm, km/h" />
          </Form.Item>
          <Form.Item name="minValue" label="最小値">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="maxValue" label="最大値">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="gain" label="ゲイン">
            <InputNumber style={{ width: '100%' }} step={0.001} />
          </Form.Item>
          <Form.Item name="offset" label="オフセット">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="conversionType" label="変換タイプ">
            <Input placeholder="例: LINEAR" />
          </Form.Item>
          <Form.Item name="description" label="説明">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
