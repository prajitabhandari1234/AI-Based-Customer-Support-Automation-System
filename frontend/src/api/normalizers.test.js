/** Unit tests for backend response normalisation. */
import { describe, expect, it } from 'vitest'
import {
  normalizeKnowledgeEntry,
  normalizeLoginResponse,
  normalizeMessage,
  normalizeNotification,
  normalizeTicketDetail,
  normalizeTicketSummary,
  normalizeUser
} from './normalizers'

describe('API normalizers', () => {
  it('normalises user id aliases and null values', () => {
    expect(normalizeUser({ userId: 3, name: 'Agent' })).toMatchObject({ id: 3, name: 'Agent' })
    expect(normalizeUser(null)).toBeNull()
  })

  it('flattens backend ticket details for the pages', () => {
    const detail = normalizeTicketDetail({
      ticket: {
        ticketId: 7,
        customer: { userId: 2, name: 'Customer' },
        assignedAgent: { userId: 3, name: 'Agent' },
        status: 'ESCALATED'
      },
      summary: { ticketId: 7, customerName: 'Customer', assignedAgentName: 'Agent' },
      messages: [{ messageId: 9, senderType: 'AI', content: 'Hello' }]
    })

    expect(detail.ticket.id).toBe(7)
    expect(detail.ticket.customerName).toBe('Customer')
    expect(detail.ticket.assignedAgentName).toBe('Agent')
    expect(detail.ticket.escalated).toBe(true)
    expect(detail.messages[0].id).toBe(9)
    expect(detail.messages[0].senderName).toBe('AI Assistant')
  })

  it('uses safe defaults for missing ticket names and message senders', () => {
    expect(normalizeTicketSummary({ id: 10 })).toMatchObject({ customerName: 'Unknown customer', escalated: false })
    expect(normalizeMessage({ id: 1, senderType: 'AGENT' }).senderName).toBe('Support Agent')
  })

  it('maps knowledge-base and notification ids returned by JPA entities', () => {
    expect(normalizeKnowledgeEntry({ kbId: 4 }).id).toBe(4)
    expect(normalizeNotification({ notificationId: 5, isRead: false, ticket: { ticketId: 8 } })).toMatchObject({
      id: 5,
      read: false,
      ticketId: 8
    })
  })

  it('creates a user object from flattened authentication responses', () => {
    expect(normalizeLoginResponse({ userId: 4, name: 'Customer', email: 'c@example.com', role: 'CLIENT' })).toMatchObject({
      user: { id: 4, name: 'Customer', role: 'CLIENT' }
    })
  })
})
