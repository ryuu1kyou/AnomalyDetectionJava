export interface CanSignal {
  id: string
  tenantId: string
  frameId: number
  name: string
  description: string
  startBit: number
  length: number
  byteOrder: string
  isSigned: boolean
  specificationId: string | null
}

export interface CreateUpdateCanSignalInput {
  frameId: number
  name: string
  description: string
  startBit: number
  length: number
  byteOrder: string
  isSigned: boolean
  specificationId: string | null
}
