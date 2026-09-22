import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, AGENT_EMAIL, AGENT_PASSWORD, login, authHeaders } from './helpers.js';

export const options = {
  vus: Number(__ENV.VUS || 20),
  iterations: Number(__ENV.ITERATIONS || 100),
  thresholds: {
    http_req_failed: ['rate<0.05'],
  },
};

export function setup() {
  if (!__ENV.TICKET_ID) {
    throw new Error('Set TICKET_ID to a ticket assigned to agent@support.local before running this test.');
  }
  return { token: login(AGENT_EMAIL, AGENT_PASSWORD), ticketId: __ENV.TICKET_ID };
}

export default function (data) {
  const statuses = ['IN_PROGRESS', 'ON_HOLD'];
  const status = statuses[__ITER % statuses.length];
  const response = http.patch(
    `${BASE_URL}/api/agent/tickets/${data.ticketId}/status`,
    JSON.stringify({ status, resolutionNotes: `concurrency-${__VU}-${__ITER}` }),
    { ...authHeaders(data.token), tags: { name: 'PATCH /api/agent/tickets/:id/status' } },
  );
  check(response, { 'concurrent update accepted': (r) => r.status === 200 });
  sleep(0.05);
}
