import {
  Badge,
  Button,
  Card,
  Drawer,
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
  asilLevel: string
  approvalStatus: string
  description?: string
  requirementId?: string
  safetyGoalId?: string
  hazardAnalysisId?: string
  detectionLogicId?: string
  projectId?: string
  version?: string
  // Traceability keys
  featureId?: string
  decisionId?: string
  changeId?: string
  ifImpact?: string
  unknownUntil?: string
  unknownOwnerId?: string
  designRationale?: string
  assumption?: string
  constraintText?: string
  docSyncStatus?: string
  scope?: string
  applicability?: string
  // M9-A extended fields
  svnRev?: string
  moduleId?: string
  ifVersion?: string
  changeType?: string
}

interface OemApprovalDto {
  id: string
  entityId: string
  entityType: string
  oemCode: string
  type: string
  status: string
  featureId?: string
  decisionId?: string
  applicability?: string
}

interface FeatureTraceabilityDto {
  featureId: string
  safetyRecords: SafetyTraceRecord[]
  oemApprovals: OemApprovalDto[]
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
  // Traceability keys
  featureId: string
  decisionId: string
  changeId: string
  ifImpact: string
  unknownUntil: string
  unknownOwnerId: string
  designRationale: string
  docSyncStatus: string
  scope: string
  applicability: string
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

const APPROVAL_STATUS_COLOR: Record<string, string> = {
  DRAFT: 'default',
  SUBMITTED: 'processing',
  UNDER_REVIEW: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
}

const IF_IMPACT_COLOR: Record<string, string> = {
  UNCHANGED: 'green',
  CHANGED: 'blue',
  UNKNOWN: 'orange',
}

const DOC_SYNC_STATUS_COLOR: Record<string, string> = {
  NOT_REQUIRED: 'default',
  PENDING: 'orange',
  UPDATED: 'processing',
  REVIEWED: 'success',
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

function useFeatureTraceability(featureId: string | null) {
  return useQuery<FeatureTraceabilityDto>({
    queryKey: ['traceability-feature', featureId],
    queryFn: () => apiFetch<FeatureTraceabilityDto>(`/app/traceability/feature/${featureId}`),
    enabled: !!featureId,
  })
}

const defaultValues: CreateSafetyTraceInput = {
  name: '', description: '', requirementId: '', safetyGoalId: '',
  hazardAnalysisId: '', asilLevel: 'QM', detectionLogicId: '', projectId: '',
  relatedDocuments: [],
  // Traceability keys
  featureId: '', decisionId: '', changeId: '',
  ifImpact: 'UNKNOWN', unknownUntil: '', unknownOwnerId: '',
  designRationale: '', docSyncStatus: 'NOT_REQUIRED',
  scope: 'PLATFORM', applicability: '',
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
  const [detailRecord, setDetailRecord] = useState<SafetyTraceRecord | null>(null)
  const [detailOpen, setDetailOpen] = useState(false)
  const [traceabilityFeatureId, setTraceabilityFeatureId] = useState<string | null>(null)
  const [traceabilityOpen, setTraceabilityOpen] = useState(false)

  const { data: featureTraceability, isLoading: traceLoading } = useFeatureTraceability(traceabilityFeatureId)

  const displayed = filter
    ? data.filter(r => r.name?.toLowerCase().includes(filter.toLowerCase())
        || r.asilLevel?.includes(filter.toUpperCase())
        || r.featureId?.toLowerCase().includes(filter.toLowerCase()))
    : data

  function openDetail(row: SafetyTraceRecord) {
    setDetailRecord(row)
    setDetailOpen(true)
  }

  function openTraceability(featureId: string) {
    setTraceabilityFeatureId(featureId)
    setTraceabilityOpen(true)
  }

  function openCreate() {
    setEditing(null)
    form.setFieldsValue(defaultValues)
    setModalOpen(true)
  }

  function openEdit(row: SafetyTraceRecord) {
    setEditing(row)
    form.setFieldsValue({
      name: row.name,
      description: row.description,
      requirementId: row.requirementId,
      safetyGoalId: row.safetyGoalId,
      hazardAnalysisId: '',
      asilLevel: row.asilLevel,
      detectionLogicId: '',
      projectId: '',
      relatedDocuments: [],
      // Traceability keys
      featureId: row.featureId ?? '',
      decisionId: row.decisionId ?? '',
      changeId: row.changeId ?? '',
      ifImpact: row.ifImpact ?? 'UNCHANGED',
      unknownUntil: row.unknownUntil ?? '',
      unknownOwnerId: row.unknownOwnerId ?? '',
      designRationale: row.designRationale ?? '',
      docSyncStatus: row.docSyncStatus ?? 'NOT_REQUIRED',
      scope: row.scope ?? 'PLATFORM',
      applicability: row.applicability ?? '',
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
    { title: '名称', dataIndex: 'name',
      render: (v: string, r: SafetyTraceRecord) => (
        <Button type="link" onClick={() => openDetail(r)}>{v}</Button>
      ),
    },
    { title: 'ASIL', dataIndex: 'asilLevel', width: 90,
      render: v => <Tag color="orange">{v}</Tag> },
    { title: 'ステータス', dataIndex: 'approvalStatus', width: 120,
      render: v => <Tag color={APPROVAL_STATUS_COLOR[v] ?? 'default'}>{v}</Tag> },
    {
      title: 'feature_id', dataIndex: 'featureId', width: 180,
      render: (v: string | undefined) => v ? (
        <Tag color="blue" style={{ cursor: 'pointer' }} onClick={() => openTraceability(v)}>
          {v}
        </Tag>
      ) : <Tag color="default">—</Tag>,
    },
    {
      title: 'IF 影響', dataIndex: 'ifImpact', width: 120,
      render: (v: string | undefined) => v ? (
        <Badge color={IF_IMPACT_COLOR[v] ?? 'default'} text={v} />
      ) : <Tag color="default">—</Tag>,
    },
    {
      title: '文書同期', dataIndex: 'docSyncStatus', width: 120,
      render: (v: string | undefined) => v ? (
        <Tag color={DOC_SYNC_STATUS_COLOR[v] ?? 'default'}>{v}</Tag>
      ) : <Tag color="default">—</Tag>,
    },
    { title: 'エンティティタイプ', dataIndex: 'entityType', width: 150 },
    {
      title: '操作', key: 'actions', width: 220,
      render: (_: unknown, row: SafetyTraceRecord) => (
        <Space>
          <Button size="small" onClick={() => openEdit(row)}>編集</Button>
          {row.approvalStatus === 'DRAFT' && (
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
        okText={editing ? '更新' : '作成'} destroyOnClose width={720}>
        <Form form={form} layout="vertical">
          <Typography.Title level={5}>基本情報</Typography.Title>
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

          <Typography.Title level={5} style={{ marginTop: 16 }}>トレーサビリティ (Phase B)</Typography.Title>
          <Form.Item name="featureId" label="feature_id" rules={[{ required: true, message: 'feature_id は必須です' }]}>
            <Input placeholder="FEAT-xxxx" />
          </Form.Item>
          <Form.Item name="decisionId" label="decision_id">
            <Input placeholder="DEC-xxxx" />
          </Form.Item>
          <Form.Item name="changeId" label="change_id">
            <Input placeholder="CHG-xxxx" />
          </Form.Item>
          <Form.Item name="ifImpact" label="IF 影響">
            <Select options={[
              { value: 'UNCHANGED', label: 'UNCHANGED' },
              { value: 'CHANGED', label: 'CHANGED' },
              { value: 'NEW_IF', label: 'NEW_IF' },
              { value: 'DELETED_IF', label: 'DELETED_IF' },
              { value: 'UNKNOWN', label: 'UNKNOWN (期限・所有者必須)' },
            ]} />
          </Form.Item>
          <Space>
            <Form.Item name="unknownUntil" label="unknown 回収期限">
              <Input type="date" placeholder="YYYY-MM-DD" />
            </Form.Item>
            <Form.Item name="unknownOwnerId" label="回収責任者 ID">
              <Input placeholder="UUID" />
            </Form.Item>
          </Space>
          <Form.Item name="designRationale" label="設計根拠 (Design Rationale)">
            <Input.TextArea rows={2} placeholder="変更理由・設計意図" />
          </Form.Item>
          <Form.Item name="docSyncStatus" label="文書同期状態">
            <Select options={[
              { value: 'NOT_REQUIRED', label: 'NOT_REQUIRED' },
              { value: 'PENDING', label: 'PENDING' },
              { value: 'UPDATED', label: 'UPDATED' },
              { value: 'REVIEWED', label: 'REVIEWED' },
            ]} />
          </Form.Item>
          <Form.Item name="scope" label="公開範囲 (Scope)">
            <Select options={[
              { value: 'PLATFORM', label: 'PLATFORM (全 OEM 共通)' },
              { value: 'OEM_SPECIFIC', label: 'OEM_SPECIFIC' },
              { value: 'INTERNAL_ONLY', label: 'INTERNAL_ONLY (社内限定)' },
            ]} />
          </Form.Item>
          <Form.Item name="applicability" label="適用範囲">
            <Input placeholder="例: All OEMs, Toyota-only" />
          </Form.Item>
        </Form>
      </Modal>

      {/* ── Detail Drawer ────────────────────────────────────────────── */}
      <Drawer open={detailOpen} onClose={() => setDetailOpen(false)}
        title="Safety トレーサビリティ詳細" width={560}>
        {detailRecord && (
          <Space direction="vertical" size="middle" style={{ display: 'flex' }}>
            <Typography.Title level={4}>{detailRecord.name}</Typography.Title>
            <Tag color={APPROVAL_STATUS_COLOR[detailRecord.approvalStatus] ?? 'default'}>{detailRecord.approvalStatus}</Tag>
            <Tag color="orange">{detailRecord.asilLevel}</Tag>

            <Card title="基本情報" size="small">
              <p><strong>要求仕様 ID:</strong> {detailRecord.requirementId ?? '—'}</p>
              <p><strong>安全目標 ID:</strong> {detailRecord.safetyGoalId ?? '—'}</p>
              <p><strong>説明:</strong> {detailRecord.description ?? '—'}</p>
            </Card>

            <Card title="トレーサビリティキー (Phase B)" size="small">
              <p><strong>feature_id:</strong>{' '}
                {detailRecord.featureId ? (
                  <Tag color="blue" style={{ cursor: 'pointer' }}
                    onClick={() => { setDetailOpen(false); openTraceability(detailRecord.featureId!) }}>
                    {detailRecord.featureId}
                  </Tag>
                ) : '—'}</p>
              <p><strong>decision_id:</strong> {detailRecord.decisionId ?? '—'}</p>
              <p><strong>change_id:</strong> {detailRecord.changeId ?? '—'}</p>
              <p><strong>IF 影響:</strong>{' '}
                {detailRecord.ifImpact ? (
                  <Tag color={IF_IMPACT_COLOR[detailRecord.ifImpact] ?? 'default'}>
                    {detailRecord.ifImpact}
                  </Tag>
                ) : '—'}</p>
              {detailRecord.ifImpact === 'UNKNOWN' && (
                <>
                  <p><strong>unknown 回収期限:</strong> {detailRecord.unknownUntil ?? '未設定'}</p>
                  <p><strong>回収責任者:</strong> {detailRecord.unknownOwnerId ?? '未設定'}</p>
                </>
              )}
              <p><strong>設計根拠:</strong> {detailRecord.designRationale ?? '—'}</p>
              <p><strong>前提:</strong> {detailRecord.assumption ?? '—'}</p>
              <p><strong>制約:</strong> {detailRecord.constraintText ?? '—'}</p>
              <p><strong>文書同期:</strong>{' '}
                <Tag color={DOC_SYNC_STATUS_COLOR[detailRecord.docSyncStatus ?? ''] ?? 'default'}>
                  {detailRecord.docSyncStatus ?? '—'}
                </Tag></p>
              <p><strong>公開範囲:</strong> {detailRecord.scope ?? '—'}</p>
              <p><strong>適用範囲:</strong> {detailRecord.applicability ?? '—'}</p>
            </Card>
          </Space>
        )}
      </Drawer>

      {/* ── Cross-module Traceability Drawer (OEM ↔ Safety) ───────────── */}
      <Drawer open={traceabilityOpen} onClose={() => setTraceabilityOpen(false)}
        title={`feature_id: ${traceabilityFeatureId} — 横断突合`} width={720}>
        {traceLoading && <Typography.Text type="secondary">読み込み中…</Typography.Text>}
        {featureTraceability && (
          <Space direction="vertical" size="large" style={{ display: 'flex' }}>
            <Card title={`Safety レコード (${featureTraceability.safetyRecords.length})`} size="small">
              {featureTraceability.safetyRecords.length === 0 ? (
                <Typography.Text type="secondary">該当 Safety レコードなし</Typography.Text>
              ) : (
                featureTraceability.safetyRecords.map(r => (
                  <Tag key={r.id} color="orange">{r.name ?? r.title} ({r.asilLevel})</Tag>
                ))
              )}
            </Card>
            <Card title={`OEM 承認 (${featureTraceability.oemApprovals.length})`} size="small">
              {featureTraceability.oemApprovals.length === 0 ? (
                <Typography.Text type="secondary">該当 OEM 承認なし</Typography.Text>
              ) : (
                <Table
                  dataSource={featureTraceability.oemApprovals}
                  rowKey="id"
                  size="small"
                  pagination={false}
                  columns={[
                    { title: 'OEM', dataIndex: 'oemCode' },
                    { title: '種別', dataIndex: 'type' },
                    { title: 'ステータス', dataIndex: 'status',
                      render: v => <Tag color={APPROVAL_STATUS_COLOR[v] ?? 'default'}>{v}</Tag> },
                    { title: 'decision_id', dataIndex: 'decisionId',
                      render: v => v ?? '—' },
                    { title: '適用範囲', dataIndex: 'applicability',
                      render: v => v ?? '—' },
                  ]}
                />
              )}
            </Card>
          </Space>
        )}
      </Drawer>
    </Space>
  )
}
