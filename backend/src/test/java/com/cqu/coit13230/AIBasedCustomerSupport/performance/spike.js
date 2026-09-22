import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from './helpers.js';

export const options = {
  stages: [
    { duration: '20s', target: 10 },
    { duration: '10s', target: 250 },
    { duration: '30s', target: 250 },
    { duration: '10s', target: 10 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.10'],
    http_req_duration: ['p(95)<3000'],
  },
};

export default function () {
  const response = http.get(`${BASE_URL}/actuator/health`, { tags: { name: 'GET /actuator/health' } });
  check(response, { 'health survives spike': (r) => r.status === 200 });
  sleep(0.1);
}
