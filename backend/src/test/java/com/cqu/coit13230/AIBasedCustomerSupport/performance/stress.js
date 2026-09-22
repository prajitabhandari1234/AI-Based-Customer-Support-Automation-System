import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, CUSTOMER_EMAIL, CUSTOMER_PASSWORD, login, authHeaders } from './helpers.js';

export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '2m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '2m', target: 150 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2500'],
  },
};

export function setup() {
  return { token: login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD) };
}

export default function (data) {
  const params = authHeaders(data.token);
  const summary = http.get(`${BASE_URL}/api/tickets/my/summary`, {
    ...params,
    tags: { name: 'GET /api/tickets/my/summary' },
  });
  check(summary, { 'summary works under stress': (r) => r.status === 200 });

  if (__ITER % 5 === 0) {
    const chat = http.post(
      `${BASE_URL}/api/chat/messages`,
      JSON.stringify({ message: `I have an account login problem ${__VU}-${__ITER}` }),
      { ...params, tags: { name: 'POST /api/chat/messages' } },
    );
    check(chat, { 'chat works under stress': (r) => r.status === 200 });
  }
  sleep(0.2);
}
