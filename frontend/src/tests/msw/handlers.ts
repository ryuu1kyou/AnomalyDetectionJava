import { http, HttpResponse } from 'msw'

const BASE = '/api/app'

export const canSignalHandlers = [
  http.get(`${BASE}/can-signals`, () =>
    HttpResponse.json([
      { id: 'sig-1', frameId: 256, name: 'EngineSpeed', description: 'Engine RPM',
        startBit: 0, length: 16, byteOrder: 'LITTLE_ENDIAN', signed: false, specificationId: null },
    ])
  ),
  http.post(`${BASE}/can-signals`, async ({ request }) => {
    const body = await request.json() as Record<string, unknown>
    return HttpResponse.json({ id: 'sig-new', ...body }, { status: 201 })
  }),
  http.delete(`${BASE}/can-signals/:id`, () => new HttpResponse(null, { status: 204 })),
]

export const detectionTemplateHandlers = [
  http.get(`${BASE}/detection-templates`, () =>
    HttpResponse.json([
      { id: 'tpl-1', name: 'Threshold Exceeded', description: 'Value over threshold',
        expression: 'value > threshold', threshold: 100.0, isActive: true },
    ])
  ),
]

export const handlers = [...canSignalHandlers, ...detectionTemplateHandlers]
