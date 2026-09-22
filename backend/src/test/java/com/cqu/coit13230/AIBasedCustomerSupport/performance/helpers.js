import http from 'k6/http';
import { check } from 'k6';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const CUSTOMER_EMAIL = __ENV.CUSTOMER_EMAIL || 'customer@support.local';
export const CUSTOMER_PASSWORD = __ENV.CUSTOMER_PASSWORD || 'Customer123!';
export const AGENT_EMAIL = __ENV.AGENT_EMAIL || 'agent@support.local';
export const AGENT_PASSWORD = __ENV.AGENT_PASSWORD || 'Agent123!';
export const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@support.local';
export const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'Admin123!';

export function login(email, password) {
  const response = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({ email, password }), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'POST /api/auth/login' },
  });
  check(response, {
    'login succeeded': (r) => r.status === 200,
    'login returned token': (r) => !!r.json('token'),
  });
  return response.json('token');
}

export function authHeaders(token) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };
}

export function expectStatus(response, expected, name) {
  check(response, {
    [`${name} status ${expected}`]: (r) => r.status === expected,
  });
}
