import { Card, Descriptions, Space, Tabs, Tag, Typography } from 'antd'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import dayjs from 'dayjs'
import { projectsApi } from '../api/projectsApi'
import { ProjectPriority, ProjectStatus } from '../models/project'
import type { Project } from '../models/project'

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

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      try {
        const p = projectId ? await projectsApi.getById(projectId) : undefined
        if (!cancelled) setProject(p ?? null)
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
              <Card>
                <Typography.Text type="secondary">
                  TBD: 移植元の milestone UI を参考に実装します。
                </Typography.Text>
              </Card>
            ),
          },
          {
            key: 'members',
            label: 'メンバー',
            children: (
              <Card>
                <Typography.Text type="secondary">
                  TBD: Project members 管理 UI を実装します。
                </Typography.Text>
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
    </Space>
  )
}
