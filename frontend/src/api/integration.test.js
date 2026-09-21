/** Integration tests for API wrappers using mocked HTTP responses. */
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { chatApi, notificationApi, ticketApi } from './client'

function installBrowserStubs() {
  const values = new Map()
  globalThis.localStorage = {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, String(value)),
    removeItem: (key) => values.delete(key),
    clear: () => values.clear()
  }
  globalThis.window = globalThis
  window.dispatchEvent = () => true
}

function jsonResponse(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' }
  })
}

describe('API wrapper integration', () => {
  beforeEach(() => {
    installBrowserStubs()
    vi.restoreAllMocks()
  })

  it('creates a ticket and returns the normalised detail shape', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({
      ticket: { ticketId: 42, customer: { userId: 7, name: 'Customer' }, status: 'ESCALATED' },
      messages: [{ messageId: 1, senderType: 'CLIENT', content: 'Help' }]
    }))

    const detail = await ticketApi.create({ message: 'Help', category: 'GENERAL_INQUIRY', priority: 'MEDIUM' })
    expect(detail.ticket.id).toBe(42)
    expect(detail.ticket.customerId).toBe(7)
    expect(detail.messages[0].senderName).toBe('Customer')
  })

  it('normalises the ticket returned with an AI chat response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({
      reply: 'Here is the answer.',
      analysis: { confidence: 0.9 },
      ticket: { ticketId: 8, status: 'RESOLVED_BY_AI' }
    }))

    const result = await chatApi.send('Question')
    expect(result.reply).toBe('Here is the answer.')
    expect(result.ticket.id).toBe(8)
  })

  it('marks only unread notifications and keeps failed updates unchanged', async () => {
    const markRead = vi.spyOn(notificationApi, 'markRead')
      .mockResolvedValueOnce({ id: 1, read: true })
      .mockRejectedValueOnce(new Error('temporary failure'))

    const input = [
      { id: 1, read: false, message: 'First' },
      { id: 2, read: false, message: 'Second' },
      { id: 3, read: true, message: 'Already read' }
    ]

    const result = await notificationApi.markAllRead('CLIENT', input)
    expect(markRead).toHaveBeenCalledTimes(2)
    expect(result[0].read).toBe(true)
    expect(result[1].read).toBe(false)
    expect(result[2].read).toBe(true)
  })
})
