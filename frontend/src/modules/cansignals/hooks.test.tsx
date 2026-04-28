import { waitFor } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { renderHookWithProviders } from '../../tests/renderWithProviders'
import { useCanSignals } from './hooks'

describe('useCanSignals', () => {
  it('fetches and returns the signal list', async () => {
    const { result } = renderHookWithProviders(() => useCanSignals())

    await waitFor(() => expect(result.current.isSuccess).toBe(true))

    const signals = result.current.data!
    expect(signals).toHaveLength(1)
    expect(signals[0].name).toBe('EngineSpeed')
    expect(signals[0].frameId).toBe(256)
  })

  it('starts in loading state', () => {
    const { result } = renderHookWithProviders(() => useCanSignals())

    expect(result.current.isLoading).toBe(true)
  })
})
