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
  Switch,
  Table,
  Tag,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useState } from 'react'
import { useCanSignals, useCreateCanSignal, useDeleteCanSignal, useUpdateCanSignal } from './hooks'
import type { CanSignal, CreateUpdateCanSignalInput } from './types'

const BYTE_ORDER_OPTIONS = [
  { value: 'LITTLE_ENDIAN', label: 'Little Endian' },
  { value: 'BIG_ENDIAN', label: 'Big Endian' },
]

const defaultValues: CreateUpdateCanSignalInput = {
  frameId: 0,
  name: '',
  description: '',
  startBit: 0,
  length: 8,
  byteOrder: 'LITTLE_ENDIAN',
  isSigned: false,
  specificationId: null,
}

export default function CanSignalListPage() {
  const { data = [], isLoading } = useCanSignals()
  const createMutation = useCreateCanSignal()
  const updateMutation = useUpdateCanSignal()
  const deleteMutation = useDeleteCanSignal()

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<CanSignal | null>(null)
  const [form] = Form.useForm<CreateUpdateCanSignalInput>()
  const [filter, setFilter] = useState('')

  const displayed = filter
    ? data.filter(
        s =>
          s.name.toLowerCase().includes(filter.toLowerCase()) ||
          String(s.frameId).includes(filter)
      )
    : data

  function openCreate() {
    setEditing(null)
    form.setFieldsValue(defaultValues)
    setModalOpen(true)
  }

  function openEdit(row: CanSignal) {
    setEditing(row)
    form.setFieldsValue({
      frameId: row.frameId,
      name: row.name,
      description: row.description,
      startBit: row.startBit,
      length: row.length,
      byteOrder: row.byteOrder,
      isSigned: row.isSigned,
      specificationId: row.specificationId,
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

  const columns: ColumnsType<CanSignal> = [
    { title: 'Frame ID', dataIndex: 'frameId', width: 100, sorter: (a, b) => a.frameId - b.frameId },
    {
      title: '名称',
      dataIndex: 'name',
      render: v => <Typography.Text strong>{v}</Typography.Text>,
    },
    { title: '説明', dataIndex: 'description', ellipsis: true },
    { title: 'Start Bit', dataIndex: 'startBit', width: 90, align: 'right' },
    { title: 'Length', dataIndex: 'length', width: 80, align: 'right' },
    {
      title: 'Byte Order',
      dataIndex: 'byteOrder',
      width: 130,
      render: v => <Tag>{v}</Tag>,
    },
    {
      title: 'Signed',
      dataIndex: 'isSigned',
      width: 80,
      render: (v: boolean) => <Tag color={v ? 'blue' : 'default'}>{v ? 'Yes' : 'No'}</Tag>,
    },
    {
      title: '操作',
      key: 'actions',
      width: 140,
      render: (_: unknown, row) => (
        <Space>
          <Button size="small" onClick={() => openEdit(row)}>編集</Button>
          <Popconfirm
            title="削除しますか？"
            onConfirm={() => deleteMutation.mutate(row.id)}
            okText="削除"
            cancelText="キャンセル"
          >
            <Button size="small" danger>削除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>CAN 信号</Typography.Title>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input.Search
            placeholder="名称 / Frame ID 検索"
            allowClear
            style={{ width: 280 }}
            onSearch={setFilter}
            onChange={e => !e.target.value && setFilter('')}
          />
          <Button type="primary" onClick={openCreate}>新規作成</Button>
        </Space>

        <Table<CanSignal>
          rowKey="id"
          columns={columns}
          dataSource={displayed}
          loading={isLoading}
          pagination={{ pageSize: 20, showSizeChanger: true }}
          scroll={{ x: 900 }}
        />
      </Card>

      <Modal
        open={modalOpen}
        title={editing ? 'CAN 信号 編集' : 'CAN 信号 新規作成'}
        onCancel={() => setModalOpen(false)}
        onOk={handleOk}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        okText={editing ? '更新' : '作成'}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="frameId" label="Frame ID" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="説明">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="startBit" label="Start Bit" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item name="length" label="Length (bits)" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={1} />
          </Form.Item>
          <Form.Item name="byteOrder" label="Byte Order" rules={[{ required: true }]}>
            <Select options={BYTE_ORDER_OPTIONS} />
          </Form.Item>
          <Form.Item name="isSigned" label="Signed" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="specificationId" label="Specification ID">
            <Input placeholder="UUID (省略可)" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
