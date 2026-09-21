// The backend returns a mix of entity fields and DTO aliases. These adapters give pages one stable shape.
/**
 * Normalises the backend user identifiers into the single shape used by the frontend.
 *
 * @param {Object|null} user Raw backend user object.
 * @returns {Object|null} Normalised user.
 */
export function normalizeUser(user) {
  if (!user) return null
  return {
    ...user,
    id: user.id ?? user.userId ?? null
  }
}

/**
 * Normalises ticket summary fields and nested customer/agent values returned by different DTOs.
 *
 * @param {Object|null} ticket Raw ticket object.
 * @returns {Object|null} Normalised ticket summary.
 */
export function normalizeTicketSummary(ticket) {
  if (!ticket) return null

  const customer = normalizeUser(ticket.customer)
  const assignedAgent = normalizeUser(ticket.assignedAgent)

  return {
    ...ticket,
    id: ticket.id ?? ticket.ticketId ?? null,
    ticketId: ticket.ticketId ?? ticket.id ?? null,
    customer: customer ?? ticket.customer ?? null,
    customerId: ticket.customerId ?? customer?.id ?? null,
    customerName: ticket.customerName ?? customer?.name ?? 'Unknown customer',
    assignedAgent: assignedAgent ?? ticket.assignedAgent ?? null,
    assignedAgentId: ticket.assignedAgentId ?? assignedAgent?.id ?? null,
    assignedAgentName: ticket.assignedAgentName ?? assignedAgent?.name ?? null,
    escalated: Boolean(ticket.escalated ?? ticket.status === 'ESCALATED')
  }
}

/**
 * Normalises ticket message identifiers and creates a useful sender name when one is missing.
 *
 * @param {Object|null} message Raw message object.
 * @returns {Object|null} Normalised message.
 */
export function normalizeMessage(message) {
  if (!message) return null

  const senderUser = normalizeUser(message.senderUser)
  const senderType = message.senderType || 'SYSTEM'
  const defaultSenderName = {
    CLIENT: 'Customer',
    AI: 'AI Assistant',
    AGENT: 'Support Agent',
    SYSTEM: 'System'
  }[senderType] || senderType

  return {
    ...message,
    id: message.id ?? message.messageId ?? null,
    messageId: message.messageId ?? message.id ?? null,
    senderUser,
    senderName: message.senderName ?? senderUser?.name ?? defaultSenderName
  }
}

/**
 * Combines entity and summary ticket data into the stable detail model expected by pages.
 *
 * @param {Object|null} detail Raw ticket detail response.
 * @returns {Object|null} Normalised ticket detail.
 */
export function normalizeTicketDetail(detail) {
  if (!detail) return null

  const entityTicket = normalizeTicketSummary(detail.ticket || detail)
  const summaryTicket = normalizeTicketSummary(detail.summary)

  // Check whether the summary actually contains escalation information.
  const summaryHasEscalation = Boolean(
    detail.summary && (
      Object.prototype.hasOwnProperty.call(detail.summary, 'escalated') ||
      Object.prototype.hasOwnProperty.call(detail.summary, 'status')
    )
  )

  // Summary DTO values are preferred because they already contain flattened customer and agent names.
  const ticket = entityTicket
    ? {
        ...entityTicket,
        ...(summaryTicket || {}),
        customer: entityTicket.customer || summaryTicket?.customer || null,
        assignedAgent: entityTicket.assignedAgent || summaryTicket?.assignedAgent || null,
        // Keep the entity escalation value if the summary does not provide it.
        escalated: summaryHasEscalation
          ? summaryTicket.escalated
          : entityTicket.escalated
      }
    : summaryTicket

  return {
    ...detail,
    ticket,
    summary: summaryTicket || ticket,
    messages: Array.isArray(detail.messages) ? detail.messages.map(normalizeMessage).filter(Boolean) : []
  }
}

/**
 * Normalises knowledge-base identifiers, editor details and active state.
 *
 * @param {Object|null} entry Raw knowledge-base entry.
 * @returns {Object|null} Normalised entry.
 */
export function normalizeKnowledgeEntry(entry) {
  if (!entry) return null
  const updatedBy = normalizeUser(entry.lastUpdatedBy)

  return {
    ...entry,
    id: entry.id ?? entry.kbId ?? null,
    kbId: entry.kbId ?? entry.id ?? null,
    lastUpdatedBy: updatedBy,
    lastUpdatedByName: updatedBy?.name ?? null,
    active: entry.active !== false
  }
}

/**
 * Normalises notification identifiers, read state and optional linked ticket details.
 *
 * @param {Object|null} notification Raw notification response.
 * @returns {Object|null} Normalised notification.
 */
export function normalizeNotification(notification) {
  if (!notification) return null
  const ticket = normalizeTicketSummary(notification.ticket)

  return {
    ...notification,
    id: notification.id ?? notification.notificationId ?? null,
    notificationId: notification.notificationId ?? notification.id ?? null,
    read: Boolean(notification.read ?? notification.isRead),
    isRead: Boolean(notification.isRead ?? notification.read),
    ticket,
    ticketId: notification.ticketId ?? ticket?.id ?? null,
    user: normalizeUser(notification.user)
  }
}

/**
 * Converts login and registration responses into one consistent session response shape.
 *
 * @param {Object|null} response Raw authentication response.
 * @returns {Object|null} Response containing a normalised user.
 */
export function normalizeLoginResponse(response) {
  if (!response) return response

  const user = normalizeUser(response.user || {
    userId: response.userId,
    name: response.name,
    email: response.email,
    role: response.role,
    status: response.status
  })

  return { ...response, user }
}
