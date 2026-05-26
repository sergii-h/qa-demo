import http from 'k6/http';
import { check, sleep } from 'k6';
import { TASKS_URL, readThresholds } from './lib/config.js';

export const options = {
  scenarios: {
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 10 },
        { duration: '10s', target: 200 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    ...readThresholds,
    http_req_duration: ['p(95)<2000'],
  },
};

export default function () {
  const response = http.get(TASKS_URL, { tags: { name: 'ListTasksSpike' } });

  check(response, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(0.1);
}
