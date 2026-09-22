import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, CUSTOMER_EMAIL, CUSTOMER_PASSWORD, login, authHeaders } from './helpers.js';

export const options = {
  scenarios: {
    chat_load: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 10),
      duration: __ENV.DURATION || '2m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.03'],
    'http_req_duration{name:POST /api/chat/messages}': ['p(95)<2000', 'p(99)<4000'],
  },
};

export function setup() {
  return { token: login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD) };
}

export default function (data) {
  const payload = JSON.stringify({ message: `My app is not working and shows an error ${__VU}-${__ITER}` });
  const response = http.post(`${BASE_URL}/api/chat/messages`, payload, {
    ...authHeaders(data.token),
    tags: { name: 'POST /api/chat/messages' },
  });
  check(response, {
    'chat succeeds': (r) => r.status === 200,
    'chat returns reply': (r) => !!r.json('reply'),
    'chat returns ticket': (r) => !!r.json('ticket.ticketId'),
  });
  sleep(1);
}
