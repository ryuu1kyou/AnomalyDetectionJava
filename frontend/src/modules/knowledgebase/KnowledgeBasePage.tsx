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

interface KnowledgeArticle {
  id: string
  title: string
  category: string
  tags: string[]
  isPublished: boolean
  viewCount: number
  summary: string
}

interface CreateKnowledgeArticleInput {
  title: string
  content: string
  summary: string
  category: string
  tags: string[]
  detectionLogicId: string
  canSignalId: string
  anomalyType: string
  signalName: string
}

const KEY = ['knowledge-articles']
const BASE = '/app/knowledge-articles'

const CATEGORY_OPTIONS = [
  { value: 'GENERAL', label: '一般' },
  { value: 'DIAGNOSTIC', label: '診断' },
  { value: 'SAFETY', label: '安全' },
  { value: 'CALIBRATION', label: 'キャリブレーション' },
  { value: 'TROUBLESHOOTING', label: 'トラブルシューティング' },
]

function useArticles() {
  return useQuery({ queryKey: KEY, queryFn: () => apiFetch<KnowledgeArticle[]>(BASE) })
}

function useCreateArticle() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateKnowledgeArticleInput) =>
      apiFetch<KnowledgeArticle>(BASE, { method: 'POST', body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useUpdateArticle() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: CreateKnowledgeArticleInput }) =>
      apiFetch<KnowledgeArticle>(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function useDeleteArticle() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => apiFetch<void>(`${BASE}/${id}`, { method: 'DELETE' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

function usePublishArticle() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, publish }: { id: string; publish: boolean }) =>
      apiFetch<KnowledgeArticle>(`${BASE}/${id}/${publish ? 'publish' : 'unpublish'}`, { method: 'POST' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

const defaultValues: CreateKnowledgeArticleInput = {
  title: '', content: '', summary: '', category: 'GENERAL',
  tags: [], detectionLogicId: '', canSignalId: '', anomalyType: '', signalName: '',
}

export default function KnowledgeBasePage() {
  const { data = [], isLoading } = useArticles()
  const createMutation = useCreateArticle()
  const updateMutation = useUpdateArticle()
  const deleteMutation = useDeleteArticle()
  const publishMutation = usePublishArticle()

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<KnowledgeArticle | null>(null)
  const [form] = Form.useForm<CreateKnowledgeArticleInput>()
  const [filter, setFilter] = useState('')

  const displayed = filter
    ? data.filter(a => a.title.toLowerCase().includes(filter.toLowerCase()) ||
        a.category.toLowerCase().includes(filter.toLowerCase()))
    : data

  function openCreate() {
    setEditing(null)
    form.setFieldsValue(defaultValues)
    setModalOpen(true)
  }

  function openEdit(row: KnowledgeArticle) {
    setEditing(row)
    form.setFieldsValue({
      title: row.title, content: '', summary: row.summary ?? '',
      category: row.category, tags: row.tags ?? [],
      detectionLogicId: '', canSignalId: '', anomalyType: '', signalName: '',
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

  const columns: ColumnsType<KnowledgeArticle> = [
    { title: 'タイトル', dataIndex: 'title',
      render: v => <Typography.Text strong>{v}</Typography.Text> },
    { title: 'カテゴリ', dataIndex: 'category', width: 150,
      render: v => <Tag>{v}</Tag> },
    { title: 'タグ', dataIndex: 'tags',
      render: (tags: string[]) => tags?.map(t => <Tag key={t}>{t}</Tag>) },
    { title: '公開', dataIndex: 'isPublished', width: 80,
      render: (v: boolean) => <Tag color={v ? 'success' : 'default'}>{v ? 'Yes' : 'No'}</Tag> },
    { title: '閲覧数', dataIndex: 'viewCount', width: 80, align: 'right' },
    {
      title: '操作', key: 'actions', width: 240,
      render: (_: unknown, row) => (
        <Space>
          <Button size="small" onClick={() => openEdit(row)}>編集</Button>
          <Button size="small" type="default"
            loading={publishMutation.isPending}
            onClick={() => publishMutation.mutate({ id: row.id, publish: !row.isPublished })}>
            {row.isPublished ? '非公開' : '公開'}
          </Button>
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
      <Typography.Title level={2} style={{ margin: 0 }}>ナレッジベース</Typography.Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input.Search placeholder="タイトル / カテゴリ検索" allowClear style={{ width: 280 }}
            onSearch={setFilter} onChange={e => !e.target.value && setFilter('')} />
          <Button type="primary" onClick={openCreate}>新規作成</Button>
        </Space>
        <Table<KnowledgeArticle> rowKey="id" columns={columns} dataSource={displayed}
          loading={isLoading} pagination={{ pageSize: 20 }} scroll={{ x: 900 }} />
      </Card>

      <Modal open={modalOpen} title={editing ? '記事 編集' : '記事 新規作成'}
        onCancel={() => setModalOpen(false)} onOk={handleOk}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        okText={editing ? '更新' : '作成'} destroyOnClose width={640}>
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="タイトル" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="category" label="カテゴリ" rules={[{ required: true }]}>
            <Select options={CATEGORY_OPTIONS} />
          </Form.Item>
          <Form.Item name="summary" label="サマリ">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="content" label="本文" rules={[{ required: true }]}>
            <Input.TextArea rows={5} />
          </Form.Item>
          <Form.Item name="tags" label="タグ">
            <Select mode="tags" placeholder="タグを入力して Enter" />
          </Form.Item>
          <Form.Item name="anomalyType" label="異常タイプ">
            <Input placeholder="省略可" />
          </Form.Item>
          <Form.Item name="signalName" label="信号名">
            <Input placeholder="省略可" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
