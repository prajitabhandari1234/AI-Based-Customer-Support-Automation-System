/** Shared values mirror the enums exposed by the Spring Boot backend. */
/** @type {string[]} Ticket categories accepted by the backend. */
export const TICKET_CATEGORIES = [
  'BILLING',
  'TECHNICAL',
  'ACCOUNT',
  'GENERAL_INQUIRY',
  'REFUND',
  'ORDER_STATUS',
  'PRODUCT_INFORMATION'
]

/** @type {string[]} Supported ticket priorities. */
export const TICKET_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

/** @type {string[]} Ticket lifecycle states shown in filters and badges. */
export const TICKET_STATUSES = [
  'OPEN',
  'ESCALATED',
  'IN_PROGRESS',
  'ON_HOLD',
  'RESOLVED_BY_AI',
  'RESOLVED',
  'CLOSED'
]

export const STAFF_STATUS_OPTIONS = ['ESCALATED', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED']
export const USER_ROLES = ['CLIENT', 'AGENT', 'ADMIN']
export const USER_STATUSES = ['ACTIVE', 'INACTIVE']
