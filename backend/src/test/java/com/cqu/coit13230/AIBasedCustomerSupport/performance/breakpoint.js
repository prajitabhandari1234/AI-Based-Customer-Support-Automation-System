import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL } from './helpers.js';

export const options = {
  scenarios: {
    breakpoint: {
      executor: 'ramping-arrival-rate',
      startRate: Number(__ENV.START_RATE || 10),
      timeUnit: '1s',
      preAllocatedVUs: 100,
      maxVUs: 1000,
      stages: [
        { target: 50, duration: '1m' },
        { target: 100, duration: '1m' },
        { target: 200, duration: '1m' },
        { target: 400, duration: '1m' },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.20'],
    http_req_duration: ['p(95)<5000'],
  },
};

export default function () {
  const response = http.get(`${BASE_URL}/actuator/health`, { tags: { name: 'GET /actuator/health' } });
  check(response, { 'health returns 200': (r) => r.status === 200 });
}
