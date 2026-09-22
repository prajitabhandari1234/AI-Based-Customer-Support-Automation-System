import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, CUSTOMER_EMAIL, CUSTOMER_PASSWORD, login, authHeaders } from './helpers.js';

export const options = {
  vus: 1,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export function setup() {
  return { token: login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD) };
}

export default function (data) {
  const health = http.get(`${BASE_URL}/actuator/health`, { tags: { name: 'GET /actuator/health' } });
  check(health, { 'health is up': (r) => r.status === 200 && r.json('status') === 'UP' });

  const me = http.get(`${BASE_URL}/api/auth/me`, authHeaders(data.token));
  check(me, { 'me succeeds': (r) => r.status === 200 });
  sleep(1);
}
