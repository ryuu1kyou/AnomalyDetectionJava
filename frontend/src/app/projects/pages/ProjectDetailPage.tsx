import { Card, Descriptions, Space, Tag, Typography } from 'antd'
import { Link, useParams } from 'react-router-dom'
import { mockProjects } from '../data/mockProjects'
import { ProjectPriority, ProjectStatus } from '../models/project'

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
  const project = mockProjects.find(p => p.id === projectId)

  if (!project) {
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
          {project.projectName}
        </Typography.Title>
        <Space size="small" wrap>
          <Tag>{project.projectCode}</Tag>
          <Tag color={statusColor(project.status)}>{statusLabel(project.status)}</Tag>
          <Tag>{priorityLabel(project.priority)}</Tag>
          <Link to="/projects/list">Back to list</Link>
        </Space>
      </div>

      <Card>
        <Descriptions column={2} size="small" bordered>
          <Descriptions.Item label="OEM">{project.oemName}</Descriptions.Item>
          <Descriptions.Item label="対象市場">{project.targetMarket}</Descriptions.Item>
          <Descriptions.Item label="車両モデル">{project.vehicleModel}</Descriptions.Item>
          <Descriptions.Item label="モデル年">{project.modelYear}</Descriptions.Item>
          <Descriptions.Item label="プラットフォーム">{project.platform}</Descriptions.Item>
          <Descriptions.Item label="主要システム">{project.primarySystem}</Descriptions.Item>
          <Descriptions.Item label="開始日">{project.startDate}</Descriptions.Item>
          <Descriptions.Item label="予定終了日">{project.plannedEndDate}</Descriptions.Item>
          <Descriptions.Item label="進捗">{project.progressPercentage}%</Descriptions.Item>
          <Descriptions.Item label="異常数">{project.totalAnomalies}</Descriptions.Item>
          <Descriptions.Item label="解決済み異常">{project.resolvedAnomalies}</Descriptions.Item>
          <Descriptions.Item label="CAN 信号数">{project.totalCanSignals}</Descriptions.Item>
          <Descriptions.Item label="検出ロジック数">{project.totalDetectionLogics}</Descriptions.Item>
          <Descriptions.Item label="説明" span={2}>
            {project.description}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </Space>
  )
}
