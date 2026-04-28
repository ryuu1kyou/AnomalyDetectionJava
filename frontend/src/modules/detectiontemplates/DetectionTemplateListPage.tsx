import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Space,
  Switch,
  Table,
  Tag,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useState } from 'react'
import {
  useCreateDetectionTemplate,
  useDeleteDetectionTemplate,
  useDetectionTemplates,
  useUpdateDetectionTemplate,
} from './hooks'
import type { CreateUpdateDetectionTemplateInput, DetectionTemplate } from './types'

const defaultValues: CreateUpdateDetectionTemplateInput = {
  name: '',
  description: '',
  canSignalId: '',
  expression: '',
  threshold: null,
  isActive: true,
}

export default function DetectionTemplateListPage() {
  const { data = [], isLoading } = useDetectionTemplates()
  const createMutation = useCreateDetectionTemplate()
  const updateMutation = useUpdateDetectionTemplate()
  const deleteMutation = useDeleteDetectionTemplate()

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<DetectionTemplate | null>(null)
  const [form] = Form.useForm<CreateUpdateDetectionTemplateInput>()
  const [filter, setFilter] = useState('')

  const displayed = filter
    ? data.filter(t => t.name.toLowerCase().includes(filter.toLowerCase()))
    : data

  function openCreate() {
    setEditing(null)
    form.setFieldsValue(defaultValues)
    setModalOpen(true)
  }

  function openEdit(row: DetectionTemplate) {
    setEditing(row)
    form.setFieldsValue({
      name: row.name,
      description: row.description,
      canSignalId: row.canSignalId,
      expression: row.expression,
      threshold: row.threshold,
      isActive: row.isActive,
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

  const columns: ColumnsType<DetectionTemplate> = [
    {
      title: '名称',
      dataIndex: 'name',
      render: v => <Typography.Text strong>{v}</Typography.Text>,
    },
    { title: '説明', dataIndex: 'description', ellipsis: true },
    { title: '式 (expression)', dataIndex: 'expression', ellipsis: true },
    {
      title: '閾値',
      dataIndex: 'threshold',
      width: 100,
      align: 'right',
      render: (v: number | null) => (v !== null && v !== undefined ? v : '-'),
    },
    {
      title: '有効',
      dataIndex: 'isActive',
      width: 80,
      render: (v: boolean) => <Tag color={v ? 'success' : 'default'}>{v ? 'Yes' : 'No'}</Tag>,
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
      <Typography.Title level={2} style={{ margin: 0 }}>検出テンプレート</Typography.Title>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input.Search
            placeholder="名称で検索"
            allowClear
            style={{ width: 280 }}
            onSearch={setFilter}
            onChange={e => !e.target.value && setFilter('')}
          />
          <Button type="primary" onClick={openCreate}>新規作成</Button>
        </Space>

        <Table<DetectionTemplate>
          rowKey="id"
          columns={columns}
          dataSource={displayed}
          loading={isLoading}
          pagination={{ pageSize: 20, showSizeChanger: true }}
        />
      </Card>

      <Modal
        open={modalOpen}
        title={editing ? '検出テンプレート 編集' : '検出テンプレート 新規作成'}
        onCancel={() => setModalOpen(false)}
        onOk={handleOk}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        okText={editing ? '更新' : '作成'}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="説明">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="canSignalId" label="CAN 信号 ID" rules={[{ required: true }]}>
            <Input placeholder="UUID" />
          </Form.Item>
          <Form.Item name="expression" label="検出式" rules={[{ required: true }]}>
            <Input.TextArea rows={2} placeholder='例: value > threshold' />
          </Form.Item>
          <Form.Item name="threshold" label="閾値">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="isActive" label="有効" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
