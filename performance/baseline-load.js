import http from 'k6/http';
import { check, sleep } from 'k6';
import { TASKS_URL, readThresholds } from './lib/config.js';

export const options = {
  scenarios: {
    baseline: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 10 },
        { duration: '2m', target: 10 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: readThresholds,
};

export default function () {
  const response = http.get(TASKS_URL, { tags: { name: 'ListTasks' } });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response is json array': (r) => {
      try {
        return Array.isArray(r.json());
      } catch {
        return false;
      }
    },
  });

  sleep(1);
}
