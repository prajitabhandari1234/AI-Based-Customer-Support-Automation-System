const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

export class ApiError extends Error {
  constructor(message, status, validationErrors = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.validationErrors = validationErrors
  }
}

export async function api(path, options = {}) {
  const token = localStorage.getItem('support_token')
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  })

  if (response.status === 204) return null

  const contentType = response.headers.get('content-type') || ''
  const data = contentType.includes('application/json') ? await response.json() : await response.text()

  if (!response.ok) {
    const message = typeof data === 'object' ? data.message : data
    throw new ApiError(message || `Request failed with status ${response.status}`, response.status,
      typeof data === 'object' ? data.validationErrors : {})
  }
  return data
}

export const authApi = {
  login: (body) => api('/api/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  register: (body) => api('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  me: () => api('/api/auth/me')
}

export const ticketApi = {
  mine: () => api('/api/tickets/my'),
  mySummary: () => api('/api/tickets/my/summary'),
  detail: (id) => api(`/api/tickets/${id}`),
  create: (body) => api('/api/tickets', { method: 'POST', body: JSON.stringify(body) }),
  message: (id, message) => api(`/api/tickets/${id}/messages`, {
    method: 'POST', body: JSON.stringify({ message })
  }),
  staff: () => api('/api/agent/tickets'),
  status: (id, body) => api(`/api/agent/tickets/${id}/status`, {
    method: 'PATCH', body: JSON.stringify(body)
  }),
  assign: (id, agentId) => api(`/api/agent/tickets/${id}/assign/${agentId}`, { method: 'PATCH' })
}

export const chatApi = {
  send: (message, ticketId = null) => api('/api/chat/messages', {
    method: 'POST', body: JSON.stringify({ message, ticketId })
  })
}

export const analyticsApi = {
  summary: () => api('/api/analytics/summary')
}

export const knowledgeApi = {
  list: () => api('/api/knowledge'),
  create: (body) => api('/api/knowledge', { method: 'POST', body: JSON.stringify(body) }),
  update: (id, body) => api(`/api/knowledge/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  remove: (id) => api(`/api/knowledge/${id}`, { method: 'DELETE' })
}

export const usersApi = {
  list: () => api('/api/admin/users'),
  agents: () => api('/api/admin/users/agents'),
  update: (id, body) => api(`/api/admin/users/${id}`, {
    method: 'PATCH', body: JSON.stringify(body)
  })
}

export const notificationApi = {
  mine: () => api('/api/notifications'),
  markRead: (id) => api(`/api/notifications/${id}/read`, { method: 'PATCH' })
}
