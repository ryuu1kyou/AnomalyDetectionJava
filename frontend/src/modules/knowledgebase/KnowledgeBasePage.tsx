import { Card, Space, Table, Tag, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface KnowledgeArticle {
  id: string
  title: string
  category: string
  tags: string[]
  isPublished: boolean
  viewCount: number
}

export default function KnowledgeBasePage() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['knowledge-articles'],
    queryFn: () => apiFetch<KnowledgeArticle[]>('/app/knowledge-articles'),
  })

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>ナレッジベース</Typography.Title>
      <Card>
        <Table
          rowKey="id"
          loading={isLoading}
          dataSource={data}
          columns={[
            { title: 'タイトル', dataIndex: 'title' },
            { title: 'カテゴリ', dataIndex: 'category', width: 140 },
            {
              title: 'タグ',
              dataIndex: 'tags',
              render: (tags: string[]) => tags?.map(t => <Tag key={t}>{t}</Tag>),
            },
            {
              title: '公開',
              dataIndex: 'isPublished',
              width: 80,
              render: (v: boolean) => <Tag color={v ? 'success' : 'default'}>{v ? 'Yes' : 'No'}</Tag>,
            },
            { title: '閲覧数', dataIndex: 'viewCount', width: 80, align: 'right' },
          ]}
          pagination={{ pageSize: 20 }}
        />
      </Card>
    </Space>
  )
}
