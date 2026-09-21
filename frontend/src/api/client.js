import { API_BASE_URL, API_TIMEOUT_MS } from '../config/environment'
import {
  normalizeKnowledgeEntry,
  normalizeLoginResponse,
  normalizeNotification,
  normalizeTicketDetail,
  normalizeTicketSummary,
  normalizeUser
} from './normalizers'

/** Browser event sent when an authenticated API request returns HTTP 401. */
export const AUTH_EXPIRED_EVENT = 'ai-customer-support:auth-expired'

/** Error type used to keep HTTP status and backend validation details with API failures. */
export class ApiError extends Error {
  constructor(message, status = 0, validationErrors = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.validationErrors = validationErrors || {}
  }
}

/** Builds an API URL from the configured base URL and a request path. */
function buildUrl(path) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${API_BASE_URL}${normalizedPath}`
}

/** Converts non-empty filter values into a URL query string. */
function buildQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

/** Parses JSON/text responses and handles successful empty responses. */
async function readResponse(response) {
  if (response.status === 204) return null
  const contentType = response.headers.get('content-type') || ''

  if (contentType.includes('application/json') || contentType.includes('+json')) {
    return response.json()
  }

  return response.text()
}

/**
 * Sends one authenticated request to the backend and converts failed responses into ApiError values.
 *
 * @param {string} path Backend path beginning with /api or /actuator.
 * @param {RequestInit} [options={}] Fetch options for the request.
 * @returns {Promise<*>} Parsed JSON, text or null response.
 * @throws {ApiError} When the server rejects the request, times out or cannot be reached.
 */
export async function api(path, options = {}) {
  const token = localStorage.getItem('support_token')
  const controller = new AbortController()
  const timer = window.setTimeout(() => controller.abort(), API_TIMEOUT_MS)
  const hasBody = options.body !== undefined && options.body !== null

  try {
    const response = await fetch(buildUrl(path), {
      ...options,
      signal: options.signal || controller.signal,
      headers: {
        ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers || {})
      }
    })

    const data = await readResponse(response)

    if (!response.ok) {
      if (response.status === 401 && token) {
        window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT))
      }

      const message = typeof data === 'object' && data !== null ? data.message : data
      const validationErrors = typeof data === 'object' && data !== null ? data.validationErrors : {}
      throw new ApiError(message || `Request failed with status ${response.status}`, response.status, validationErrors)
    }

    return data
  } catch (error) {
    if (error instanceof ApiError) throw error
    if (error.name === 'AbortError') throw new ApiError('The server took too long to respond. Please try again.', 0)
    throw new ApiError('Unable to connect to the backend service. Check the API URL and server status.', 0)
  } finally {
    window.clearTimeout(timer)
  }
}

/** Authentication endpoints used by the session context. */
export const authApi = {
  login: async (body) => normalizeLoginResponse(await api('/api/auth/login', { method: 'POST', body: JSON.stringify(body) })),
  register: async (body) => normalizeLoginResponse(await api('/api/auth/register', { method: 'POST', body: JSON.stringify(body) })),
  me: async () => normalizeUser(await api('/api/auth/me'))
}

/** Customer and staff ticket endpoints. */
export const ticketApi = {
  mine: async () => (await api('/api/tickets/my')).map(normalizeTicketSummary),
  mySummary: () => api('/api/tickets/my/summary'),
  detail: async (id) => normalizeTicketDetail(await api(`/api/tickets/${id}`)),
  create: async (body) => normalizeTicketDetail(await api('/api/tickets', { method: 'POST', body: JSON.stringify(body) })),
  message: async (id, message) => normalizeTicketDetail(await api(`/api/tickets/${id}/messages`, {
    method: 'POST',
    body: JSON.stringify({ message })
  })),
  staff: async () => (await api('/api/agent/tickets')).map(normalizeTicketSummary),
  status: async (id, body) => normalizeTicketDetail(await api(`/api/agent/tickets/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify(body)
  })),
  assign: async (id, agentId) => normalizeTicketDetail(await api(`/api/agent/tickets/${id}/assign/${agentId}`, {
    method: 'PATCH'
  })),
  assignToMe: async (id) => {
    await api(`/api/agent/tickets/${id}/assign`, { method: 'PUT' })
    return ticketApi.detail(id)
  }
}

/** AI chat endpoint. */
export const chatApi = {
  send: async (message, ticketId = null) => {
    const response = await api('/api/chat/messages', {
      method: 'POST',
      body: JSON.stringify({ message, ticketId })
    })
    return {
      ...response,
      ticket: normalizeTicketSummary(response.ticket)
    }
  }
}

/** Administrative analytics and reporting endpoints. */
export const analyticsApi = {
  summary: () => api('/api/admin/analytics/tickets'),
  filter: (filters) => api(`/api/admin/analytics/tickets/filter${buildQuery(filters)}`),
  weeklyReport: (date) => api(`/api/admin/analytics/reports/weekly${buildQuery({ date })}`),
  monthlyReport: (date) => api(`/api/admin/analytics/reports/monthly${buildQuery({ date })}`)
}

/** Knowledge-base administration endpoints. */
export const knowledgeApi = {
  list: async () => (await api('/api/knowledge-base')).map(normalizeKnowledgeEntry),
  create: async (body) => normalizeKnowledgeEntry(await api('/api/knowledge-base', {
    method: 'POST',
    body: JSON.stringify(body)
  })),
  update: async (id, body) => normalizeKnowledgeEntry(await api(`/api/knowledge-base/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body)
  })),
  remove: (id) => api(`/api/knowledge-base/${id}`, { method: 'DELETE' })
}

/** Administrative user-management endpoints. */
export const usersApi = {
  list: async () => (await api('/api/admin/users')).map(normalizeUser),
  agents: async () => (await api('/api/admin/users/agents')).map(normalizeUser),
  get: async (id) => normalizeUser(await api(`/api/admin/users/${id}`)),
  create: async (body) => normalizeUser(await api('/api/admin/users', {
    method: 'POST',
    body: JSON.stringify(body)
  })),
  update: async (id, body) => normalizeUser(await api(`/api/admin/users/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(body)
  }))
}

/** Selects the customer or staff notification endpoint for the current role. */
function notificationBase(role) {
  return role === 'CLIENT' ? '/api/customer/notifications' : '/api/agent/notifications'
}

/** Role-aware notification endpoints and read-state helpers. */
export const notificationApi = {
  mine: async (role) => (await api(notificationBase(role))).map(normalizeNotification),
  markRead: async (role, id) => normalizeNotification(await api(`${notificationBase(role)}/${id}/read`, { method: 'PUT' })),
  markAllRead: async (role, notifications) => {
    const unread = notifications.filter((item) => !item.read && item.id)
    if (!unread.length) return notifications
    const updates = await Promise.allSettled(unread.map((item) => notificationApi.markRead(role, item.id)))
    const updatedById = new Map(
      updates.filter((result) => result.status === 'fulfilled').map((result) => [result.value.id, result.value])
    )
    return notifications.map((item) => updatedById.get(item.id) || item)
  }
}

/** System health and audit-log endpoints. */
export const systemApi = {
  health: () => api('/actuator/health'),
  logs: () => api('/api/system-logs')
}
