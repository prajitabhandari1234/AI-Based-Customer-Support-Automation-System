/** Unit tests for the shared HTTP client and error handling. */
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { api, ApiError, AUTH_EXPIRED_EVENT } from './client'

function installBrowserStubs() {
  const values = new Map()
  globalThis.localStorage = {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, String(value)),
    removeItem: (key) => values.delete(key),
    clear: () => values.clear()
  }

  const events = new EventTarget()
  globalThis.window = globalThis
  window.addEventListener = events.addEventListener.bind(events)
  window.removeEventListener = events.removeEventListener.bind(events)
  window.dispatchEvent = events.dispatchEvent.bind(events)
}

describe('ApiError', () => {
  it('preserves status and validation details for form handling', () => {
    const error = new ApiError('Validation failed', 400, { email: 'must be valid' })
    expect(error.name).toBe('ApiError')
    expect(error.message).toBe('Validation failed')
    expect(error.status).toBe(400)
    expect(error.validationErrors.email).toBe('must be valid')
  })
})

describe('api', () => {
  beforeEach(() => {
    installBrowserStubs()
    vi.restoreAllMocks()
  })

  it('adds the stored bearer token and parses a JSON response', async () => {
    localStorage.setItem('support_token', 'abc123')
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(
      JSON.stringify({ status: 'UP' }),
      { status: 200, headers: { 'Content-Type': 'application/json' } }
    ))

    await expect(api('/actuator/health')).resolves.toEqual({ status: 'UP' })
    expect(fetchMock).toHaveBeenCalledWith('/actuator/health', expect.objectContaining({
      headers: expect.objectContaining({ Authorization: 'Bearer abc123' })
    }))
  })

  it('does not force a JSON content type on requests without a body', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('', { status: 200 }))
    await api('/api/test')
    const [, options] = fetchMock.mock.calls[0]
    expect(options.headers['Content-Type']).toBeUndefined()
  })

  it('dispatches the auth-expired event for authenticated 401 responses', async () => {
    localStorage.setItem('support_token', 'expired')
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(
      JSON.stringify({ message: 'Expired' }),
      { status: 401, headers: { 'Content-Type': 'application/json' } }
    ))
    const listener = vi.fn()
    window.addEventListener(AUTH_EXPIRED_EVENT, listener, { once: true })

    await expect(api('/api/private')).rejects.toMatchObject({ message: 'Expired', status: 401 })
    expect(listener).toHaveBeenCalledOnce()
  })

  it('keeps backend validation errors on failed JSON responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(
      JSON.stringify({ message: 'Validation failed', validationErrors: { email: 'Invalid email' } }),
      { status: 400, headers: { 'Content-Type': 'application/json' } }
    ))
    await expect(api('/api/auth/register', { method: 'POST', body: '{}' })).rejects.toMatchObject({
      message: 'Validation failed',
      status: 400,
      validationErrors: { email: 'Invalid email' }
    })
  })

  it('converts network failures into a user-facing ApiError', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Network failed'))
    await expect(api('/api/test')).rejects.toMatchObject({
      name: 'ApiError',
      status: 0,
      message: expect.stringContaining('Unable to connect')
    })
  })
})
