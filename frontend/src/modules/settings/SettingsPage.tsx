import { Button, Card, Form, Input, Space, Typography, message } from 'antd'
import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiFetch } from '../../shared/api/apiFetch'

interface SettingForm {
  key: string
  value: string
}

function useGetSetting() {
  return useMutation({
    mutationFn: (key: string) => apiFetch<{ key: string; value: string }>(`/app/settings?key=${encodeURIComponent(key)}`),
  })
}

function useSetSetting() {
  return useMutation({
    mutationFn: (input: SettingForm) =>
      apiFetch<void>('/app/settings', { method: 'PUT', body: JSON.stringify(input) }),
  })
}

export default function SettingsPage() {
  const [getForm] = Form.useForm<{ key: string }>()
  const [setForm] = Form.useForm<SettingForm>()
  const [retrieved, setRetrieved] = useState<{ key: string; value: string } | null>(null)
  const getMutation = useGetSetting()
  const setMutation = useSetSetting()

  async function handleGet() {
    const { key } = await getForm.validateFields()
    try {
      const result = await getMutation.mutateAsync(key)
      setRetrieved(result ?? null)
    } catch {
      setRetrieved(null)
      message.error('設定が見つかりません')
    }
  }

  async function handleSet() {
    const values = await setForm.validateFields()
    await setMutation.mutateAsync(values)
    message.success('設定を保存しました')
    setForm.resetFields()
  }

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Typography.Title level={2} style={{ margin: 0 }}>設定管理</Typography.Title>

      <Card title="設定値の取得">
        <Form form={getForm} layout="inline" onFinish={handleGet}>
          <Form.Item name="key" rules={[{ required: true }]}>
            <Input placeholder="設定キー (例: app.maxItems)" style={{ width: 300 }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={getMutation.isPending}>取得</Button>
          </Form.Item>
        </Form>
        {retrieved && (
          <Typography.Paragraph style={{ marginTop: 12 }}>
            <strong>{retrieved.key}</strong> = <Typography.Text code>{retrieved.value}</Typography.Text>
          </Typography.Paragraph>
        )}
      </Card>

      <Card title="設定値の保存 (グローバル)">
        <Form form={setForm} layout="vertical" onFinish={handleSet} style={{ maxWidth: 480 }}>
          <Form.Item name="key" label="キー" rules={[{ required: true }]}>
            <Input placeholder="例: app.maxItems" />
          </Form.Item>
          <Form.Item name="value" label="値" rules={[{ required: true }]}>
            <Input placeholder="例: 100" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={setMutation.isPending}>保存</Button>
          </Form.Item>
        </Form>
      </Card>
    </Space>
  )
}
