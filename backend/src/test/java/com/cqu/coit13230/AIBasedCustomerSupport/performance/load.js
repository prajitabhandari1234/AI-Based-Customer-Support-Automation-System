import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, CUSTOMER_EMAIL, CUSTOMER_PASSWORD, login, authHeaders } from './helpers.js';

export const options = {
  stages: [
    { duration: '1m', target: 10 },
    { duration: '3m', target: 25 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<1200', 'p(99)<2500'],
  },
};

export function setup() {
  return { token: login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD) };
}

export default function (data) {
  const params = authHeaders(data.token);
  const responses = http.batch([
    ['GET', `${BASE_URL}/api/tickets/my`, null, { ...params, tags: { name: 'GET /api/tickets/my' } }],
    ['GET', `${BASE_URL}/api/tickets/my/summary`, null, { ...params, tags: { name: 'GET /api/tickets/my/summary' } }],
    ['GET', `${BASE_URL}/api/notifications`, null, { ...params, tags: { name: 'GET /api/notifications' } }],
    ['GET', `${BASE_URL}/api/knowledge-base`, null, { ...params, tags: { name: 'GET /api/knowledge-base' } }],
  ]);
  responses.forEach((r) => check(r, { 'read endpoint succeeded': (x) => x.status === 200 }));
  sleep(0.5);
}
