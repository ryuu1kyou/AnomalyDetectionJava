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
import {
  useAnomalyDetectionLogics,
  useCreateAnomalyDetectionLogic,
  useDeleteAnomalyDetectionLogic,
  useSubmitForApproval,
  useUpdateAnomalyDetectionLogic,
} from './hooks'
import type {
  AnomalyDetectionLogic,
  AnomalyType,
  AsilLevel,
  CreateUpdateAnomalyDetectionLogicInput,
  DetectionLogicStatus,
  ImplementationType,
  LogicComplexity,
  SharingLevel,
} from './types'

const STATUS_OPTIONS: { value: DetectionLogicStatus; label: string; color: string }[] = [
  { value: 'DRAFT', label: 'ドラフト', color: 'default' },
  { value: 'PENDING_APPROVAL', label: '承認待ち', color: 'warning' },
  { value: 'APPROVED', label: '承認済', color: 'success' },
  { value: 'REJECTED', label: '却下', color: 'error' },
  { value: 'DEPRECATED', label: '非推奨', color: 'default' },
]

const ANOMALY_TYPE_OPTIONS: { value: AnomalyType; label: string }[] = [
  { value: 'TIMEOUT', label: 'タイムアウト' },
  { value: 'OUT_OF_RANGE', label: '範囲外' },
  { value: 'RATE_OF_CHANGE', label: '変化率' },
  { value: 'STUCK', label: 'スタック' },
  { value: 'PERIODIC_ANOMALY', label: '周期異常' },
  { value: 'DATA_LOSS', label: 'データロス' },
  { value: 'NOISE', label: 'ノイズ' },
  { value: 'PATTERN_ANOMALY', label: 'パターン異常' },
  { value: 'CORRELATION_ANOMALY', label: '相関異常' },
  { value: 'CUSTOM', label: 'カスタム' },
]

const ASIL_OPTIONS: { value: AsilLevel; label: string }[] = [
  { value: 'QM', label: 'QM' },
  { value: 'A', label: 'ASIL-A' },
  { value: 'B', label: 'ASIL-B' },
  { value: 'C', label: 'ASIL-C' },
  { value: 'D', label: 'ASIL-D' },
]

const COMPLEXITY_OPTIONS: { value: LogicComplexity; label: string }[] = [
  { value: 'SIMPLE', label: '単純' },
  { value: 'MEDIUM', label: '中程度' },
  { value: 'COMPLEX', label: '複雑' },
]

const IMPL_TYPE_OPTIONS: { value: ImplementationType; label: string }[] = [
  { value: 'CONFIGURATION', label: '設定' },
  { value: 'SCRIPT', label: 'スクリプト' },
  { value: 'SOURCE_CODE', label: 'ソースコード' },
  { value: 'COMPILED_CODE', label: 'コンパイル済み' },
  { value: 'TEMPLATE', label: 'テンプレート' },
]

const SHARING_OPTIONS: { value: SharingLevel; label: string }[] = [
  { value: 'PRIVATE', label: 'プライベート' },
  { value: 'OEM_PARTNER', label: 'OEM パートナー' },
  { value: 'INDUSTRY', label: '業界共有' },
  { value: 'PUBLIC', label: '公開' },
]

const defaultValues: CreateUpdateAnomalyDetectionLogicInput = {
  name: '',
  version: '1.0.0',
  oemCode: '',
  anomalyType: 'TIMEOUT',
  description: '',
  targetSystemType: '',
  complexity: 'SIMPLE',
  requirements: '',
  implementationType: 'CONFIGURATION',
  implementationContent: '',
  implementationLanguage: '',
  implementationEntryPoint: '',
  asilLevel: 'QM',
  safetyRequirementId: '',
  safetyGoalId: '',
  hazardAnalysisId: '',
  sharingLevel: 'PRIVATE',
  vehiclePhaseId: null,
}

function statusTag(status: DetectionLogicStatus) {
  const opt = STATUS_OPTIONS.find(o => o.value === status)
  return <Tag color={opt?.color}>{opt?.label ?? status}</Tag>
}

export default function AnomalyDetectionListPage() {
  const [statusFilter, setStatusFilter] = useState<DetectionLogicStatus | undefined>()
  const { data = [], isLoading } = useAnomalyDetectionLogics(statusFilter)
  const createMutation = useCreateAnomalyDetectionLogic()
  const updateMutation = useUpdateAnomalyDetectionLogic()
  const deleteMutation = useDeleteAnomalyDetectionLogic()
  const submitMutation = useSubmitForApproval()

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<AnomalyDetectionLogic | null>(null)
  const [form] = Form.useForm<CreateUpdateAnomalyDetectionLogicInput>()
  const [nameFilter, setNameFilter] = useState('')

  const displayed = nameFilter
    ? data.filter(d => d.name.toLowerCase().includes(nameFilter.toLowerCase()))
    : data

  function openCreate() {
    setEditing(null)
    form.setFieldsValue(defaultValues)
    setModalOpen(true)
  }

  function openEdit(row: AnomalyDetectionLogic) {
    setEditing(row)
    form.setFieldsValue({
      name: row.name,
      version: row.version,
      oemCode: row.oemCode,
      anomalyType: row.anomalyType,
      description: row.description,
      targetSystemType: row.targetSystemType,
      complexity: row.complexity,
      requirements: row.requirements,
      implementationType: row.implementationType,
      implementationContent: '',
      implementationLanguage: row.implementationLanguage,
      implementationEntryPoint: '',
      asilLevel: row.asilLevel,
      safetyRequirementId: row.safetyRequirementId,
      safetyGoalId: row.safetyGoalId,
      hazardAnalysisId: '',
      sharingLevel: row.sharingLevel,
      vehiclePhaseId: row.vehiclePhaseId,
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

  const columns: ColumnsType<AnomalyDetectionLogic> = [
    {
      title: '名称',
      dataIndex: 'name',
      render: v => <Typography.Text strong>{v}</Typography.Text>,
    },
    { title: 'バージョン', dataIndex: 'version', width: 90 },
    { title: 'OEM', dataIndex: 'oemCode', width: 100 },
    {
      title: '異常タイプ',
      dataIndex: 'anomalyType',
      width: 130,
      render: (v: AnomalyType) => (
        <Tag>{ANOMALY_TYPE_OPTIONS.find(o => o.value === v)?.label ?? v}</Tag>
      ),
    },
    {
      title: 'ASIL',
      dataIndex: 'asilLevel',
      width: 80,
      render: (v: AsilLevel) => <Tag color={v === 'QM' ? 'default' : 'orange'}>{v}</Tag>,
    },
    {
      title: 'ステータス',
      dataIndex: 'status',
      width: 120,
      render: (v: DetectionLogicStatus) => statusTag(v),
    },
    { title: '実行回数', dataIndex: 'executionCount', width: 90, align: 'right' },
    {
      title: '操作',
      key: 'actions',
      width: 220,
      render: (_: unknown, row) => (
        <Space>
          <Button size="small" onClick={() => openEdit(row)}>編集</Button>
          {row.status === 'DRAFT' && (
            <Button
              size="small"
              onClick={() => submitMutation.mutate(row.id)}
              loading={submitMutation.isPending}
            >
              承認申請
            </Button>
          )}
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
      <Typography.Title level={2} style={{ margin: 0 }}>異常検出ロジック</Typography.Title>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input.Search
            placeholder="名称で検索"
            allowClear
            style={{ width: 240 }}
            onSearch={setNameFilter}
            onChange={e => !e.target.value && setNameFilter('')}
          />
          <Select<DetectionLogicStatus>
            allowClear
            placeholder="ステータスフィルタ"
            style={{ width: 160 }}
            options={STATUS_OPTIONS}
            onChange={v => setStatusFilter(v)}
          />
          <Button type="primary" onClick={openCreate}>新規作成</Button>
        </Space>

        <Table<AnomalyDetectionLogic>
          rowKey="id"
          columns={columns}
          dataSource={displayed}
          loading={isLoading}
          pagination={{ pageSize: 20, showSizeChanger: true }}
          scroll={{ x: 1000 }}
        />
      </Card>

      <Modal
        open={modalOpen}
        title={editing ? '検出ロジック 編集' : '検出ロジック 新規作成'}
        onCancel={() => setModalOpen(false)}
        onOk={handleOk}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        okText={editing ? '更新' : '作成'}
        destroyOnClose
        width={640}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="version" label="バージョン" rules={[{ required: true }]}>
            <Input placeholder="1.0.0" />
          </Form.Item>
          <Form.Item name="oemCode" label="OEM コード">
            <Input />
          </Form.Item>
          <Form.Item name="anomalyType" label="異常タイプ" rules={[{ required: true }]}>
            <Select options={ANOMALY_TYPE_OPTIONS} />
          </Form.Item>
          <Form.Item name="description" label="説明">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="targetSystemType" label="対象システムタイプ">
            <Input />
          </Form.Item>
          <Form.Item name="complexity" label="複雑度" rules={[{ required: true }]}>
            <Select options={COMPLEXITY_OPTIONS} />
          </Form.Item>
          <Form.Item name="asilLevel" label="ASIL レベル" rules={[{ required: true }]}>
            <Select options={ASIL_OPTIONS} />
          </Form.Item>
          <Form.Item name="implementationType" label="実装タイプ" rules={[{ required: true }]}>
            <Select options={IMPL_TYPE_OPTIONS} />
          </Form.Item>
          <Form.Item name="implementationLanguage" label="実装言語">
            <Input placeholder="Python / JavaScript / etc." />
          </Form.Item>
          <Form.Item name="implementationContent" label="実装内容">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="sharingLevel" label="共有レベル" rules={[{ required: true }]}>
            <Select options={SHARING_OPTIONS} />
          </Form.Item>
          <Form.Item name="requirements" label="要件">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
