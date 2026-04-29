import {
  Button,
  Card,
  Checkbox,
  Col,
  Descriptions,
  Form,
  Input,
  InputNumber,
  List,
  Row,
  Space,
  Spin,
  Table,
  Tag,
  Tabs,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useState } from 'react'
import { apiFetch } from '../../shared/api/apiFetch'

interface SimilarSignalResult {
  signalId: string
  signalName: string
  similarityScore: number
  frameIdSimilarity: number
  nameSimilarity: number
  lengthSimilarity: number
  matchedAttributes: string[]
  differences: string[]
  recommendationLevel: string
  recommendationReason: string
}

interface TestDataComparison {
  sourceSignalId: string
  targetSignalId: string
  overallSimilarityScore: number
  summary: string
  sourceResultCount: number
  targetResultCount: number
  thresholdDifferences: string[]
  conditionDifferences: string[]
  resultDifferences: string[]
  recommendations: string[]
}

interface SearchForm {
  targetSignalId: string
  minimumSimilarity: number
  maxResults: number
  compareFrameId: boolean
  compareSignalName: boolean
  compareLength: boolean
}

interface RecommendForm {
  signalId: string
  maxRecommendations: number
}

interface CompareForm {
  sourceSignalId: string
  targetSignalId: string
  maxResults: number
}

const BASE = '/app/similar-pattern-search'

function levelColor(level: string): string {
  switch (level) {
    case 'Highly_Recommended':
    case 'HighlyRecommended':
      return 'success'
    case 'Recommended':
      return 'processing'
    case 'Neutral':
      return 'default'
    case 'NotRecommended':
    case 'Not_Recommended':
      return 'warning'
    default:
      return 'default'
  }
}

const resultColumns: ColumnsType<SimilarSignalResult> = [
  { title: 'シグナル名', dataIndex: 'signalName', width: 160 },
  {
    title: '類似度',
    dataIndex: 'similarityScore',
    width: 90,
    render: (v: number) => `${(v * 100).toFixed(1)}%`,
  },
  {
    title: 'Frame ID',
    dataIndex: 'frameIdSimilarity',
    width: 80,
    render: (v: number) => `${(v * 100).toFixed(0)}%`,
  },
  {
    title: '名前',
    dataIndex: 'nameSimilarity',
    width: 80,
    render: (v: number) => `${(v * 100).toFixed(0)}%`,
  },
  {
    title: '長さ',
    dataIndex: 'lengthSimilarity',
    width: 80,
    render: (v: number) => `${(v * 100).toFixed(0)}%`,
  },
  {
    title: '推奨',
    dataIndex: 'recommendationLevel',
    width: 140,
    render: (v: string) => <Tag color={levelColor(v)}>{v}</Tag>,
  },
  {
    title: '一致属性',
    dataIndex: 'matchedAttributes',
    render: (v: string[]) => (
      <Space size={2} wrap>
        {v.map(a => (
          <Tag key={a} color="blue" style={{ fontSize: 11 }}>
            {a}
          </Tag>
        ))}
      </Space>
    ),
  },
  { title: '推奨理由', dataIndex: 'recommendationReason' },
]

function SearchTab() {
  const [form] = Form.useForm<SearchForm>()
  const [results, setResults] = useState<SimilarSignalResult[]>([])
  const [loading, setLoading] = useState(false)

  async function handleSearch() {
    const values = await form.validateFields()
    setLoading(true)
    try {
      const data = await apiFetch<SimilarSignalResult[]>(`${BASE}/signals`, {
        method: 'POST',
        body: JSON.stringify({
          targetSignalId: values.targetSignalId,
          candidateSignalIds: [],
          minimumSimilarity: values.minimumSimilarity,
          maxResults: values.maxResults,
          compareFrameId: values.compareFrameId,
          compareSignalName: values.compareSignalName,
          compareLength: values.compareLength,
        }),
      })
      setResults(data)
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card title="検索条件">
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            minimumSimilarity: 0.5,
            maxResults: 10,
            compareFrameId: true,
            compareSignalName: true,
            compareLength: true,
          }}
        >
          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="targetSignalId"
                label="対象シグナル ID (UUID)"
                rules={[{ required: true, message: 'UUID を入力してください' }]}
              >
                <Input placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
              </Form.Item>
            </Col>
            <Col xs={12} md={6}>
              <Form.Item name="minimumSimilarity" label="最小類似度">
                <InputNumber min={0} max={1} step={0.05} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={12} md={6}>
              <Form.Item name="maxResults" label="最大件数">
                <InputNumber min={1} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={24}>
            <Col>
              <Form.Item name="compareFrameId" valuePropName="checked">
                <Checkbox>Frame ID を比較</Checkbox>
              </Form.Item>
            </Col>
            <Col>
              <Form.Item name="compareSignalName" valuePropName="checked">
                <Checkbox>シグナル名を比較</Checkbox>
              </Form.Item>
            </Col>
            <Col>
              <Form.Item name="compareLength" valuePropName="checked">
                <Checkbox>長さを比較</Checkbox>
              </Form.Item>
            </Col>
          </Row>
          <Button type="primary" onClick={() => void handleSearch()} loading={loading}>
            検索
          </Button>
        </Form>
      </Card>

      {results.length > 0 && (
        <Card title={`検索結果 (${results.length} 件)`}>
          <Table<SimilarSignalResult>
            rowKey="signalId"
            columns={resultColumns}
            dataSource={results}
            pagination={false}
            scroll={{ x: 900 }}
          />
        </Card>
      )}
    </Space>
  )
}

function RecommendTab() {
  const [form] = Form.useForm<RecommendForm>()
  const [results, setResults] = useState<SimilarSignalResult[]>([])
  const [loading, setLoading] = useState(false)

  async function handleRecommend() {
    const values = await form.validateFields()
    setLoading(true)
    try {
      const data = await apiFetch<SimilarSignalResult[]>(
        `${BASE}/recommendations/${encodeURIComponent(values.signalId)}?maxRecommendations=${values.maxRecommendations}`
      )
      setResults(data)
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card title="レコメンデーション">
        <Form form={form} layout="vertical" initialValues={{ maxRecommendations: 10 }}>
          <Row gutter={16}>
            <Col xs={24} md={16}>
              <Form.Item
                name="signalId"
                label="シグナル ID (UUID)"
                rules={[{ required: true }]}
              >
                <Input placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="maxRecommendations" label="最大件数">
                <InputNumber min={1} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Button type="primary" onClick={() => void handleRecommend()} loading={loading}>
            取得
          </Button>
        </Form>
      </Card>

      {loading && <Spin />}
      {results.length > 0 && (
        <Card title={`レコメンデーション (${results.length} 件)`}>
          <Table<SimilarSignalResult>
            rowKey="signalId"
            columns={resultColumns}
            dataSource={results}
            pagination={false}
            scroll={{ x: 900 }}
          />
        </Card>
      )}
    </Space>
  )
}

function CompareTab() {
  const [form] = Form.useForm<CompareForm>()
  const [result, setResult] = useState<TestDataComparison | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleCompare() {
    const values = await form.validateFields()
    setLoading(true)
    try {
      const data = await apiFetch<TestDataComparison>(`${BASE}/test-data/compare`, {
        method: 'POST',
        body: JSON.stringify(values),
      })
      setResult(data)
    } catch {
      setResult(null)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card title="テストデータ比較">
        <Form form={form} layout="vertical" initialValues={{ maxResults: 10 }}>
          <Row gutter={16}>
            <Col xs={24} md={10}>
              <Form.Item name="sourceSignalId" label="比較元シグナル ID" rules={[{ required: true }]}>
                <Input placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
              </Form.Item>
            </Col>
            <Col xs={24} md={10}>
              <Form.Item name="targetSignalId" label="比較先シグナル ID" rules={[{ required: true }]}>
                <Input placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
              </Form.Item>
            </Col>
            <Col xs={24} md={4}>
              <Form.Item name="maxResults" label="最大件数">
                <InputNumber min={1} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Button type="primary" onClick={() => void handleCompare()} loading={loading}>
            比較
          </Button>
        </Form>
      </Card>

      {loading && <Spin />}
      {result && (
        <Card title="比較結果">
          <Descriptions column={2} size="small" bordered>
            <Descriptions.Item label="総合類似度">
              {(result.overallSimilarityScore * 100).toFixed(1)}%
            </Descriptions.Item>
            <Descriptions.Item label="サマリー" span={2}>
              {result.summary}
            </Descriptions.Item>
            <Descriptions.Item label="比較元 件数">{result.sourceResultCount}</Descriptions.Item>
            <Descriptions.Item label="比較先 件数">{result.targetResultCount}</Descriptions.Item>
          </Descriptions>

          {result.thresholdDifferences.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <Typography.Text strong>閾値の差異</Typography.Text>
              <List
                size="small"
                dataSource={result.thresholdDifferences}
                renderItem={item => <List.Item>{item}</List.Item>}
              />
            </div>
          )}

          {result.recommendations.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <Typography.Text strong>推奨事項</Typography.Text>
              <List
                size="small"
                dataSource={result.recommendations}
                renderItem={item => <List.Item>{item}</List.Item>}
              />
            </div>
          )}
        </Card>
      )}
    </Space>
  )
}

export default function SimilarPatternSearchPage() {
  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <div>
        <Typography.Title level={2} style={{ margin: 0 }}>
          類似パターン検索
        </Typography.Title>
        <Typography.Text type="secondary">
          CAN シグナルの類似検索・テストデータ比較・レコメンデーション
        </Typography.Text>
      </div>

      <Tabs
        items={[
          { key: 'search', label: '類似シグナル検索', children: <SearchTab /> },
          { key: 'recommend', label: 'レコメンデーション', children: <RecommendTab /> },
          { key: 'compare', label: 'テストデータ比較', children: <CompareTab /> },
        ]}
      />
    </Space>
  )
}
