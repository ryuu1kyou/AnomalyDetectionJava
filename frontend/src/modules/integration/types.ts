export type IntegrationType =
  | 'RestApi'
  | 'GraphQL'
  | 'Mqtt'
  | 'WebSocket'
  | 'Database'
  | 'FileSystem'

export interface IntegrationEndpoint {
  id: string
  tenantId: string
  name: string
  description: string
  type: IntegrationType
  baseUrl: string
  endpointUrl: string
  isActive: boolean
  timeout: number
  requireAuthentication: boolean
  authenticationScheme: string
  lastSyncDate: string | null
  successCount: number
  failureCount: number
}

export interface CreateIntegrationEndpointInput {
  name: string
  description: string
  type: IntegrationType
  baseUrl: string
  endpointUrl: string
  apiKey: string
  isActive: boolean
  timeout: number
  requireAuthentication: boolean
  authenticationScheme: string
}
