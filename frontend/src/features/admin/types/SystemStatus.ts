export interface IntegrationStatus {
  name: string
  configured: boolean
  detail: string | null
}

export interface SystemStatus {
  catalogProviders: IntegrationStatus[]
  ollama: IntegrationStatus
  mqtt: IntegrationStatus
}
