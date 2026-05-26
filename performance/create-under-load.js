import http from 'k6/http';
import { check, sleep } from 'k6';
import { TASKS_URL, writeThresholds } from './lib/config.js';
import { buildTaskPayload, jsonHeaders, uniqueTitle } from './lib/tasks.js';

export const options = {
  scenarios: {
    create: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 5 },
        { duration: '2m', target: 5 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: writeThresholds,
};

export default function () {
  const payload = buildTaskPayload(uniqueTitle('perf-create'));
  const response = http.post(TASKS_URL, payload, {
    ...jsonHeaders,
    tags: { name: 'CreateTask' },
  });

  check(response, {
    'status is 201': (r) => r.status === 201,
    'response has id': (r) => {
      try {
        return typeof r.json('id') === 'string' && r.json('id').length > 0;
      } catch {
        return false;
      }
    },
  });

  sleep(1);
}
