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
import {
  useCreateIntegrationEndpoint,
  useDeleteIntegrationEndpoint,
  useIntegrationEndpoints,
  useTestConnection,
} from './hooks'
import type { CreateIntegrationEndpointInput, IntegrationEndpoint, IntegrationType } from './types'

const TYPE_OPTIONS: { value: IntegrationType; label: string }[] = [
  { value: 'RestApi', label: 'REST API' },
  { value: 'GraphQL', label: 'GraphQL' },
  { value: 'Mqtt', label: 'MQTT' },
  { value: 'WebSocket', label: 'WebSocket' },
  { value: 'Database', label: 'Database' },
  { value: 'FileSystem', label: 'File System' },
]

const defaultValues: CreateIntegrationEndpointInput = {
  name: '',
  description: '',
  type: 'RestApi',
  baseUrl: '',
  endpointUrl: '',
  apiKey: '',
  isActive: true,
  timeout: 30,
  requireAuthentication: false,
  authenticationScheme: '',
}

export default function IntegrationListPage() {
  const { data = [], isLoading } = useIntegrationEndpoints()
  const createMutation = useCreateIntegrationEndpoint()
  const deleteMutation = useDeleteIntegrationEndpoint()
  const testMutation = useTestConnection()

  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm<CreateIntegrationEndpointInput>()

  async function handleOk() {
    const values = await form.validateFields()
    await createMutation.mutateAsync(values)
    setModalOpen(false)
  }

  const columns: ColumnsType<IntegrationEndpoint> = [
    {
      title: '名称',
      dataIndex: 'name',
      render: v => <Typography.Text strong>{v}</Typography.Text>,
    },
    {
      title: 'タイプ',
      dataIndex: 'type',
      width: 110,
      render: (v: IntegrationType) => (
        <Tag>{TYPE_OPTIONS.find(o => o.value === v)?.label ?? v}</Tag>
      ),
    },
    { title: 'Base URL', dataIndex: 'baseUrl', ellipsis: true },
    {
      title: '有効',
      dataIndex: 'isActive',
      width: 80,
      render: (v: boolean) => <Tag color={v ? 'success' : 'default'}>{v ? 'Yes' : 'No'}</Tag>,
    },
    {
      title: '成功/失敗',
      key: 'counts',
      width: 100,
      align: 'right',
      render: (_: unknown, row) => (
        <Typography.Text>
          {row.successCount}/{row.failureCount}
        </Typography.Text>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      width: 180,
      render: (_: unknown, row) => (
        <Space>
          <Button
            size="small"
            loading={testMutation.isPending}
            onClick={() => testMutation.mutate(row.id)}
          >
            接続テスト
          </Button>
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
      <Typography.Title level={2} style={{ margin: 0 }}>インテグレーション</Typography.Title>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            onClick={() => {
              form.setFieldsValue(defaultValues)
              setModalOpen(true)
            }}
          >
            エンドポイント追加
          </Button>
        </Space>

        <Table<IntegrationEndpoint>
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={isLoading}
          pagination={{ pageSize: 20 }}
        />
      </Card>

      <Modal
        open={modalOpen}
        title="エンドポイント 新規作成"
        onCancel={() => setModalOpen(false)}
        onOk={handleOk}
        confirmLoading={createMutation.isPending}
        okText="作成"
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="説明">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="type" label="タイプ" rules={[{ required: true }]}>
            <Select options={TYPE_OPTIONS} />
          </Form.Item>
          <Form.Item name="baseUrl" label="Base URL" rules={[{ required: true }]}>
            <Input placeholder="https://api.example.com" />
          </Form.Item>
          <Form.Item name="endpointUrl" label="Endpoint URL">
            <Input placeholder="/v1/data" />
          </Form.Item>
          <Form.Item name="apiKey" label="API キー">
            <Input.Password />
          </Form.Item>
          <Form.Item name="timeout" label="タイムアウト (秒)">
            <InputNumber style={{ width: '100%' }} min={1} />
          </Form.Item>
          <Form.Item name="requireAuthentication" label="認証必須" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="isActive" label="有効" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
