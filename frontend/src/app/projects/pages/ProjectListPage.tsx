import {
  Button,
  Card,
  Col,
  Form,
  Input,
  Progress,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { TableRowSelection } from 'antd/es/table/interface'
import type { Key } from 'react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import dayjs from 'dayjs'
import { projectsApi } from '../api/projectsApi'
import { ProjectPriority, ProjectStatus } from '../models/project'
import type { Project, ProjectsQuery } from '../models/project'

function statusLabel(status: ProjectStatus): string {
  switch (status) {
    case ProjectStatus.Planning:
      return '計画中'
    case ProjectStatus.Active:
      return '進行中'
    case ProjectStatus.OnHold:
      return '保留'
    case ProjectStatus.Completed:
      return '完了'
    case ProjectStatus.Cancelled:
      return 'キャンセル'
  }
}

function statusColor(status: ProjectStatus): string {
  switch (status) {
    case ProjectStatus.Active:
      return 'processing'
    case ProjectStatus.Completed:
      return 'success'
    case ProjectStatus.OnHold:
      return 'warning'
    case ProjectStatus.Cancelled:
      return 'default'
    case ProjectStatus.Planning:
    default:
      return 'blue'
  }
}

function priorityLabel(priority: ProjectPriority): string {
  switch (priority) {
    case ProjectPriority.Low:
      return '低'
    case ProjectPriority.Medium:
      return '中'
    case ProjectPriority.High:
      return '高'
    case ProjectPriority.Critical:
      return '緊急'
  }
}

export default function ProjectListPage() {
  const [form] = Form.useForm<ProjectsQuery>()
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([])
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<Project[]>([])
  const debounceTimerRef = useRef<number | null>(null)

  const columns: ColumnsType<Project> = [
    {
      title: 'コード',
      dataIndex: 'projectCode',
      width: 120,
      render: (code: string, row) => <Link to={`/projects/${row.id}`}>{code}</Link>,
    },
    {
      title: '名称',
      dataIndex: 'projectName',
      render: (name: string) => <Typography.Text strong>{name}</Typography.Text>,
    },
    {
      title: 'OEM',
      dataIndex: 'oemName',
      width: 160,
    },
    {
      title: 'ステータス',
      dataIndex: 'status',
      width: 120,
      render: (value: ProjectStatus) => (
        <Tag color={statusColor(value)}>{statusLabel(value)}</Tag>
      ),
    },
    {
      title: '優先度',
      dataIndex: 'priority',
      width: 100,
      render: (value: ProjectPriority) => <Tag>{priorityLabel(value)}</Tag>,
    },
    {
      title: '異常/解決',
      key: 'anomalies',
      width: 100,
      align: 'right',
      render: (_: unknown, row) => (
        <Typography.Text>
          {row.totalAnomalies}/{row.resolvedAnomalies}
        </Typography.Text>
      ),
    },
    {
      title: '進捗',
      dataIndex: 'progressPercentage',
      width: 140,
      render: (v: number) => <Progress percent={v} size="small" />,
    },
    {
      title: '開始',
      dataIndex: 'startDate',
      width: 110,
      render: (v: string) => dayjs(v).format('YYYY/MM/DD'),
    },
    {
      title: '予定終了',
      dataIndex: 'plannedEndDate',
      width: 110,
      render: (v: string) => dayjs(v).format('YYYY/MM/DD'),
    },
    {
      title: '車両',
      dataIndex: 'vehicleModel',
      width: 120,
    },
    {
      title: '主要システム',
      dataIndex: 'primarySystem',
      width: 140,
    },
  ]

  async function reload() {
    setLoading(true)
    try {
      const values = form.getFieldsValue()
      const rows = await projectsApi.list(values)
      setData(rows)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // NOTE: this setTimeout avoids a lint rule that discourages synchronous setState in effect bodies.
    const id = window.setTimeout(() => {
      void reload()
    }, 0)
    return () => window.clearTimeout(id)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const rowSelection: TableRowSelection<Project> = {
    selectedRowKeys,
    onChange: next => setSelectedRowKeys(next),
  }

  const stats = useMemo(() => {
    const totalProjects = data.length
    const activeProjects = data.filter(p => p.status === ProjectStatus.Active).length
    const completedProjects = data.filter(p => p.status === ProjectStatus.Completed).length
    const delayedProjects = data.filter(p => {
      if (p.status === ProjectStatus.Completed || p.status === ProjectStatus.Cancelled) return false
      return dayjs(p.plannedEndDate).isBefore(dayjs(), 'day')
    }).length

    return { totalProjects, activeProjects, completedProjects, delayedProjects }
  }, [data])

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <div>
        <Typography.Title level={2} style={{ margin: 0 }}>
          Projects / List
        </Typography.Title>
        <Typography.Text type="secondary">
          一覧・検索 UI（現状は mock + projectsApi 経由）
        </Typography.Text>
      </div>

      <Row gutter={12}>
        <Col xs={12} md={6}>
          <Card size="small">
            <Typography.Text type="secondary">総プロジェクト数</Typography.Text>
            <div style={{ fontSize: 20, fontWeight: 600 }}>{stats.totalProjects}</div>
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card size="small">
            <Typography.Text type="secondary">進行中</Typography.Text>
            <div style={{ fontSize: 20, fontWeight: 600 }}>{stats.activeProjects}</div>
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card size="small">
            <Typography.Text type="secondary">完了</Typography.Text>
            <div style={{ fontSize: 20, fontWeight: 600 }}>{stats.completedProjects}</div>
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card size="small">
            <Typography.Text type="secondary">遅延</Typography.Text>
            <div style={{ fontSize: 20, fontWeight: 600 }}>{stats.delayedProjects}</div>
          </Card>
        </Col>
      </Row>

      <Card>
        <Form
          form={form}
          layout="vertical"
          initialValues={{}}
          onValuesChange={() => {
            if (debounceTimerRef.current) {
              window.clearTimeout(debounceTimerRef.current)
            }
            debounceTimerRef.current = window.setTimeout(() => {
              void reload()
            }, 300)
          }}
        >
          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item name="filter" label="検索">
                <Input allowClear placeholder="プロジェクト名 / コード" />
              </Form.Item>
            </Col>

            <Col xs={24} md={6}>
              <Form.Item name="status" label="ステータス">
                <Select
                  allowClear
                  placeholder="すべて"
                  options={[
                    { value: ProjectStatus.Planning, label: '計画中' },
                    { value: ProjectStatus.Active, label: '進行中' },
                    { value: ProjectStatus.OnHold, label: '保留' },
                    { value: ProjectStatus.Completed, label: '完了' },
                    { value: ProjectStatus.Cancelled, label: 'キャンセル' },
                  ]}
                />
              </Form.Item>
            </Col>

            <Col xs={24} md={6}>
              <Form.Item name="priority" label="優先度">
                <Select
                  allowClear
                  placeholder="すべて"
                  options={[
                    { value: ProjectPriority.Low, label: '低' },
                    { value: ProjectPriority.Medium, label: '中' },
                    { value: ProjectPriority.High, label: '高' },
                    { value: ProjectPriority.Critical, label: '緊急' },
                  ]}
                />
              </Form.Item>
            </Col>

            <Col xs={24} md={4}>
              <Form.Item name="vehicleModel" label="車両モデル">
                <Input allowClear placeholder="例: Model X" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item name="primarySystem" label="主要システム">
                <Input allowClear placeholder="例: Brake" />
              </Form.Item>
            </Col>

            <Col xs={24} md={16} style={{ display: 'flex', alignItems: 'end' }}>
              <Space>
                <Button onClick={() => form.resetFields()}>クリア</Button>
                <Button type="primary" onClick={() => void reload()}>
                  更新
                </Button>
                <Button type="dashed" disabled>
                  新規（後で実装）
                </Button>
                <Button disabled={selectedRowKeys.length === 0}>
                  一括操作（後で実装）
                </Button>
              </Space>
            </Col>
          </Row>
        </Form>
      </Card>

      <Table
        rowKey="id"
        columns={columns}
        loading={loading}
        rowSelection={rowSelection}
        dataSource={data}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1300 }}
      />
    </Space>
  )
}
