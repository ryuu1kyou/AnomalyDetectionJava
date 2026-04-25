import { Card, Col, Form, Input, Row, Select, Space, Table, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link } from 'react-router-dom'
import { mockProjects } from '../data/mockProjects'
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
      title: '進捗',
      dataIndex: 'progressPercentage',
      width: 80,
      align: 'right',
      render: (v: number) => `${v}%`,
    },
  ]

  // skeleton implementation: filter in-memory
  const query = Form.useWatch([], form) as ProjectsQuery | undefined
  const rows = mockProjects.filter(p => {
    const f = (query?.filter ?? '').trim().toLowerCase()
    if (f) {
      const hay = `${p.projectCode} ${p.projectName}`.toLowerCase()
      if (!hay.includes(f)) return false
    }
    if (query?.status !== undefined && query?.status !== null) {
      if (p.status !== query.status) return false
    }
    if (query?.priority !== undefined && query?.priority !== null) {
      if (p.priority !== query.priority) return false
    }
    if (query?.vehicleModel) {
      if (!p.vehicleModel.toLowerCase().includes(query.vehicleModel.toLowerCase())) return false
    }
    if (query?.primarySystem) {
      if (!p.primarySystem.toLowerCase().includes(query.primarySystem.toLowerCase())) return false
    }
    return true
  })

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <div>
        <Typography.Title level={2} style={{ margin: 0 }}>
          Projects / List
        </Typography.Title>
        <Typography.Text type="secondary">
          一覧・検索 UI のスケルトン（現状は mock データ）
        </Typography.Text>
      </div>

      <Card>
        <Form form={form} layout="vertical" initialValues={{}}>
          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item name="filter" label="検索">
                <Input allowClear placeholder="プロジェクト名 / コード" />
              </Form.Item>
            </Col>

            <Col xs={24} md={6}>
              <Form.Item name="status" label="ステータス">
                <Select allowClear placeholder="すべて"
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
                <Select allowClear placeholder="すべて"
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
          </Row>
        </Form>
      </Card>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={rows}
        pagination={{ pageSize: 10 }}
      />
    </Space>
  )
}
