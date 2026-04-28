import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'

function makeQueryClient() {
  return new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
}

function Wrapper({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider client={makeQueryClient()}>
      {children}
    </QueryClientProvider>
  )
}

export function renderWithProviders(ui: ReactNode) {
  return render(ui, { wrapper: Wrapper })
}

export function renderHookWithProviders<T>(hook: () => T) {
  return renderHook(hook, { wrapper: Wrapper })
}
