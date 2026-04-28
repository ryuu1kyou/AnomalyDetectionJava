import { describe, expect, it } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../../tests/msw/server'
import { apiFetch } from './apiFetch'

describe('apiFetch', () => {
  it('returns parsed JSON on 200', async () => {
    server.use(
      http.get('/api/test', () => HttpResponse.json({ ok: true }))
    )

    const result = await apiFetch<{ ok: boolean }>('/test')

    expect(result.ok).toBe(true)
  })

  it('returns undefined on 204', async () => {
    server.use(
      http.delete('/api/test/1', () => new HttpResponse(null, { status: 204 }))
    )

    const result = await apiFetch<void>('/test/1', { method: 'DELETE' })

    expect(result).toBeUndefined()
  })

  it('throws on non-ok status', async () => {
    server.use(
      http.get('/api/not-found', () =>
        HttpResponse.json({ message: 'Not found' }, { status: 404 })
      )
    )

    await expect(apiFetch('/not-found')).rejects.toThrow('HTTP 404')
  })
})
