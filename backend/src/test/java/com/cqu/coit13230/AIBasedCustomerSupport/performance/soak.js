import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, CUSTOMER_EMAIL, CUSTOMER_PASSWORD, login, authHeaders } from './helpers.js';

export const options = {
  vus: Number(__ENV.VUS || 20),
  duration: __ENV.DURATION || '30m',
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<1500'],
  },
};

export function setup() {
  return { token: login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD) };
}

export default function (data) {
  const params = authHeaders(data.token);
  const response = http.get(`${BASE_URL}/api/tickets/my`, {
    ...params,
    tags: { name: 'GET /api/tickets/my' },
  });
  check(response, { 'ticket list remains healthy': (r) => r.status === 200 });

  if (__ITER % 60 === 0) {
    const chat = http.post(
      `${BASE_URL}/api/chat/messages`,
      JSON.stringify({ message: `My order tracking has a problem ${__VU}-${__ITER}` }),
      { ...params, tags: { name: 'POST /api/chat/messages' } },
    );
    check(chat, { 'periodic write remains healthy': (r) => r.status === 200 });
  }
  sleep(1);
}
