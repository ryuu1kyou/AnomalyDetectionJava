import {
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  InputNumber,
  Modal,
  Progress,
  Select,
  Space,
  Switch,
  Table,
  Tabs,
  Tag,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import dayjs from 'dayjs'
import { projectsApi } from '../api/projectsApi'
import { MilestoneStatus, ProjectPriority, ProjectStatus } from '../models/project'
import type {
  CreateProjectMemberDto,
  CreateProjectMilestoneDto,
  Project,
  ProjectMember,
  ProjectMilestone,
  UpdateProjectMemberDto,
  UpdateProjectMilestoneDto,
} from '../models/project'

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

export default function ProjectDetailPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const [project, setProject] = useState<Project | null>(null)
  const [loading, setLoading] = useState(true)

  const [milestones, setMilestones] = useState<ProjectMilestone[]>([])
  const [members, setMembers] = useState<ProjectMember[]>([])

  const [milestoneModalOpen, setMilestoneModalOpen] = useState(false)
  const [editingMilestone, setEditingMilestone] = useState<ProjectMilestone | null>(null)
  const [milestoneSaving, setMilestoneSaving] = useState(false)
  const [milestoneForm] = Form.useForm<CreateProjectMilestoneDto & Partial<UpdateProjectMilestoneDto>>()

  const [memberModalOpen, setMemberModalOpen] = useState(false)
  const [editingMember, setEditingMember] = useState<ProjectMember | null>(null)
  const [memberSaving, setMemberSaving] = useState(false)
  const [memberForm] = Form.useForm<CreateProjectMemberDto & Partial<UpdateProjectMemberDto>>()

  async function loadChildren(pid: string) {
    const [ms, mb] = await Promise.all([
      projectsApi.getMilestones(pid),
      projectsApi.getMembers(pid),
    ])
    setMilestones(ms)
    setMembers(mb)
  }

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      try {
        const p = projectId ? await projectsApi.getById(projectId) : undefined
        if (!cancelled) setProject(p ?? null)

        if (!cancelled && projectId) {
          await loadChildren(projectId)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    void load()
    return () => {
      cancelled = true
    }
  }, [projectId])

  if (!projectId) {
    return (
      <Card>
        <Typography.Text>Missing projectId</Typography.Text>
      </Card>
    )
  }

  if (!project && !loading) {
    return (
      <Card>
        <Typography.Text>Project not found: {projectId}</Typography.Text>
      </Card>
    )
  }

  const milestoneColumns: ColumnsType<ProjectMilestone> = [
    {
      title: '名称',
      dataIndex: 'name',
      render: (v: string) => <Typography.Text strong>{v}</Typography.Text>,
    },
    { title: '予定日', dataIndex: 'plannedDate', width: 120, render: v => dayjs(v).format('YYYY/MM/DD') },
    { title: '実績日', dataIndex: 'actualDate', width: 120, render: v => (v ? dayjs(v).format('YYYY/MM/DD') : '-') },
    {
      title: 'ステータス',
      dataIndex: 'status',
      width: 120,
      render: (s: MilestoneStatus) => {
        const label =
          s === MilestoneStatus.NotStarted
            ? '未開始'
            : s === MilestoneStatus.InProgress
              ? '進行中'
              : s === MilestoneStatus.Completed
                ? '完了'
                : s === MilestoneStatus.Delayed
                  ? '遅延'
                  : 'キャンセル'
        const color = s === MilestoneStatus.Completed ? 'success' : s === MilestoneStatus.Delayed ? 'error' : 'default'
        return <Tag color={color}>{label}</Tag>
      },
    },
    {
      title: '進捗',
      dataIndex: 'progressPercentage',
      width: 160,
      render: (v: number) => <Progress percent={v} size="small" />,
    },
    {
      title: '操作',
      key: 'actions',
      width: 220,
      render: (_: unknown, row) => (
        <Space>
          <Button
            size="small"
            onClick={() => {
              setEditingMilestone(row)
              milestoneForm.setFieldsValue({
                projectId: row.projectId,
                name: row.name,
                description: row.description,
                plannedDate: row.plannedDate,
                actualDate: row.actualDate,
                status: row.status,
                progressPercentage: row.progressPercentage,
                dependencies: row.dependencies,
                deliverables: row.deliverables,
              })
              setMilestoneModalOpen(true)
            }}
          >
            編集
          </Button>
          <Button
            size="small"
            disabled={row.status === MilestoneStatus.Completed}
            onClick={async () => {
              await projectsApi.completeMilestone(row.id)
              if (projectId) await loadChildren(projectId)
            }}
          >
            完了
          </Button>
          <Button
            size="small"
            danger
            onClick={async () => {
              await projectsApi.deleteMilestone(row.id)
              if (projectId) await loadChildren(projectId)
            }}
          >
            削除
          </Button>
        </Space>
      ),
    },
  ]

  const memberColumns: ColumnsType<ProjectMember> = [
    { title: 'ユーザー', dataIndex: 'userName', render: v => <Typography.Text strong>{v}</Typography.Text> },
    { title: 'メール', dataIndex: 'email' },
    { title: '役割', dataIndex: 'role', width: 160 },
    {
      title: '状態',
      dataIndex: 'isActive',
      width: 120,
      render: (v: boolean) => <Tag color={v ? 'success' : 'default'}>{v ? 'アクティブ' : '非アクティブ'}</Tag>,
    },
    {
      title: '操作',
      key: 'actions',
      width: 180,
      render: (_: unknown, row) => (
        <Space>
          <Button
            size="small"
            onClick={() => {
              setEditingMember(row)
              memberForm.setFieldsValue({
                projectId: row.projectId,
                userId: row.userId,
                role: row.role,
                responsibilities: row.responsibilities,
                canEdit: row.canEdit,
                canDelete: row.canDelete,
                canManageMembers: row.canManageMembers,
                isActive: row.isActive,
              })
              setMemberModalOpen(true)
            }}
          >
            編集
          </Button>
          <Button
            size="small"
            danger
            onClick={async () => {
              await projectsApi.removeMember(row.id)
              if (projectId) await loadChildren(projectId)
            }}
          >
            削除
          </Button>
        </Space>
      ),
    },
  ]

  const milestoneTotals = {
    count: milestones.length,
    completed: milestones.filter(m => m.status === MilestoneStatus.Completed).length,
  }

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <div>
        <Typography.Title level={2} style={{ margin: 0 }}>
          {project?.projectName ?? 'Loading...'}
        </Typography.Title>
        <Space size="small" wrap>
          {project && (
            <>
              <Tag>{project.projectCode}</Tag>
              <Tag color={statusColor(project.status)}>{statusLabel(project.status)}</Tag>
              <Tag>{priorityLabel(project.priority)}</Tag>
            </>
          )}
          <Link to="/projects/list">Back to list</Link>
        </Space>
      </div>

      <Tabs
        items={[
          {
            key: 'overview',
            label: '概要',
            children: (
              <Card loading={loading}>
                {project && (
                  <Descriptions column={2} size="small" bordered>
                    <Descriptions.Item label="OEM">{project.oemName}</Descriptions.Item>
                    <Descriptions.Item label="対象市場">{project.targetMarket}</Descriptions.Item>
                    <Descriptions.Item label="車両モデル">{project.vehicleModel}</Descriptions.Item>
                    <Descriptions.Item label="モデル年">{project.modelYear}</Descriptions.Item>
                    <Descriptions.Item label="プラットフォーム">{project.platform}</Descriptions.Item>
                    <Descriptions.Item label="主要システム">{project.primarySystem}</Descriptions.Item>
                    <Descriptions.Item label="開始日">
                      {dayjs(project.startDate).format('YYYY/MM/DD')}
                    </Descriptions.Item>
                    <Descriptions.Item label="予定終了日">
                      {dayjs(project.plannedEndDate).format('YYYY/MM/DD')}
                    </Descriptions.Item>
                    <Descriptions.Item label="進捗">{project.progressPercentage}%</Descriptions.Item>
                    <Descriptions.Item label="異常数">{project.totalAnomalies}</Descriptions.Item>
                    <Descriptions.Item label="解決済み異常">{project.resolvedAnomalies}</Descriptions.Item>
                    <Descriptions.Item label="CAN 信号数">{project.totalCanSignals}</Descriptions.Item>
                    <Descriptions.Item label="検出ロジック数">{project.totalDetectionLogics}</Descriptions.Item>
                    <Descriptions.Item label="説明" span={2}>
                      {project.description}
                    </Descriptions.Item>
                  </Descriptions>
                )}
              </Card>
            ),
          },
          {
            key: 'milestones',
            label: 'マイルストーン',
            children: (
              <Card
                title={
                  <Space>
                    <span>マイルストーン</span>
                    <Tag>
                      {milestoneTotals.completed}/{milestoneTotals.count}
                    </Tag>
                  </Space>
                }
                extra={
                  <Button
                    type="primary"
                    onClick={() => {
                      setEditingMilestone(null)
                      milestoneForm.resetFields()
                      milestoneForm.setFieldsValue({
                        projectId: projectId!,
                        name: '',
                        description: '',
                        plannedDate: dayjs().format('YYYY-MM-DD'),
                        dependencies: [],
                        deliverables: [],
                      })
                      setMilestoneModalOpen(true)
                    }}
                    disabled={!projectId}
                  >
                    追加
                  </Button>
                }
              >
                <Table<ProjectMilestone>
                  rowKey="id"
                  columns={milestoneColumns}
                  dataSource={milestones}
                  pagination={false}
                />
              </Card>
            ),
          },
          {
            key: 'members',
            label: 'メンバー',
            children: (
              <Card
                title="メンバー"
                extra={
                  <Button
                    type="primary"
                    onClick={() => {
                      setEditingMember(null)
                      memberForm.resetFields()
                      memberForm.setFieldsValue({
                        projectId: projectId!,
                        userId: '',
                        role: 'Engineer',
                        responsibilities: [],
                        canEdit: false,
                        canDelete: false,
                        canManageMembers: false,
                      })
                      setMemberModalOpen(true)
                    }}
                    disabled={!projectId}
                  >
                    追加
                  </Button>
                }
              >
                <Table<ProjectMember>
                  rowKey="id"
                  columns={memberColumns}
                  dataSource={members}
                  pagination={false}
                />
              </Card>
            ),
          },
          {
            key: 'stats',
            label: '統計',
            children: (
              <Card>
                <Typography.Text type="secondary">
                  TBD: Project statistics / progress / timeline を実装します。
                </Typography.Text>
              </Card>
            ),
          },
        ]}
      />

      {/* Milestone Modal */}
      <Modal
        open={milestoneModalOpen}
        onCancel={() => setMilestoneModalOpen(false)}
        title={editingMilestone ? 'マイルストーン編集' : 'マイルストーン追加'}
        confirmLoading={milestoneSaving}
        okText={editingMilestone ? '更新' : '作成'}
        onOk={async () => {
          const values = await milestoneForm.validateFields()
          setMilestoneSaving(true)
          try {
            if (editingMilestone) {
              await projectsApi.updateMilestone(editingMilestone.id, {
                name: values.name,
                description: values.description,
                plannedDate: values.plannedDate,
                actualDate: values.actualDate,
                status: values.status ?? MilestoneStatus.NotStarted,
                progressPercentage: values.progressPercentage ?? 0,
                dependencies: values.dependencies ?? [],
                deliverables: values.deliverables ?? [],
              })
            } else {
              await projectsApi.createMilestone({
                projectId: projectId!,
                name: values.name,
                description: values.description,
                plannedDate: values.plannedDate,
                dependencies: values.dependencies ?? [],
                deliverables: values.deliverables ?? [],
              })
            }
            if (projectId) await loadChildren(projectId)
            setMilestoneModalOpen(false)
          } finally {
            setMilestoneSaving(false)
          }
        }}
      >
        <Form form={milestoneForm} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="説明">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="plannedDate" label="予定日" rules={[{ required: true }]}>
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>

          <Form.Item name="dependencies" label="依存マイルストーン (ID)">
            <Select mode="tags" tokenSeparators={[',']} placeholder="例: ms-xxxx" />
          </Form.Item>

          <Form.Item name="deliverables" label="成果物">
            <Select mode="tags" tokenSeparators={[',']} placeholder="例: Doc, Spec" />
          </Form.Item>

          {editingMilestone && (
            <>
              <Form.Item name="actualDate" label="実績日">
                <Input placeholder="YYYY-MM-DD" />
              </Form.Item>
              <Form.Item name="status" label="ステータス" initialValue={MilestoneStatus.NotStarted}>
                <Select
                  options={[
                    { value: MilestoneStatus.NotStarted, label: '未開始' },
                    { value: MilestoneStatus.InProgress, label: '進行中' },
                    { value: MilestoneStatus.Completed, label: '完了' },
                    { value: MilestoneStatus.Delayed, label: '遅延' },
                    { value: MilestoneStatus.Cancelled, label: 'キャンセル' },
                  ]}
                />
              </Form.Item>
              <Form.Item name="progressPercentage" label="進捗率 (%)" initialValue={0}>
                <InputNumber min={0} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </>
          )}
        </Form>
      </Modal>

      {/* Member Modal */}
      <Modal
        open={memberModalOpen}
        onCancel={() => setMemberModalOpen(false)}
        title={editingMember ? 'メンバー編集' : 'メンバー追加'}
        confirmLoading={memberSaving}
        okText={editingMember ? '更新' : '追加'}
        onOk={async () => {
          const values = await memberForm.validateFields()
          setMemberSaving(true)
          try {
            if (editingMember) {
              await projectsApi.updateMember(editingMember.id, {
                role: values.role,
                responsibilities: values.responsibilities ?? [],
                canEdit: values.canEdit ?? false,
                canDelete: values.canDelete ?? false,
                canManageMembers: values.canManageMembers ?? false,
                isActive: values.isActive ?? true,
              })
            } else {
              await projectsApi.addMember({
                projectId: projectId!,
                userId: values.userId,
                role: values.role,
                responsibilities: values.responsibilities ?? [],
                canEdit: values.canEdit ?? false,
                canDelete: values.canDelete ?? false,
                canManageMembers: values.canManageMembers ?? false,
              })
            }
            if (projectId) await loadChildren(projectId)
            setMemberModalOpen(false)
          } finally {
            setMemberSaving(false)
          }
        }}
      >
        <Form form={memberForm} layout="vertical">
          {!editingMember && (
            <Form.Item name="userId" label="ユーザーID" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
          )}

          <Form.Item name="role" label="役割" rules={[{ required: true }]}>
            <Select
              options={[
                { value: 'ProjectManager', label: 'プロジェクトマネージャー' },
                { value: 'TechnicalLead', label: 'テクニカルリード' },
                { value: 'Engineer', label: 'エンジニア' },
                { value: 'QualityAssurance', label: '品質保証' },
                { value: 'Reviewer', label: 'レビュアー' },
                { value: 'Observer', label: 'オブザーバー' },
              ]}
            />
          </Form.Item>

          {editingMember && (
            <Form.Item name="isActive" label="アクティブ" valuePropName="checked" initialValue={true}>
              <Switch />
            </Form.Item>
          )}

          <Form.Item name="responsibilities" label="責任範囲">
            <Select mode="tags" tokenSeparators={[',']} placeholder="例: Planning, Review" />
          </Form.Item>

          <Form.Item name="canEdit" label="編集権限" valuePropName="checked" initialValue={false}>
            <Switch />
          </Form.Item>
          <Form.Item name="canDelete" label="削除権限" valuePropName="checked" initialValue={false}>
            <Switch />
          </Form.Item>
          <Form.Item
            name="canManageMembers"
            label="メンバー管理権限"
            valuePropName="checked"
            initialValue={false}
          >
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
