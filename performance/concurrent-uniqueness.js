import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';
import { TASKS_URL } from './lib/config.js';
import { buildTaskPayload, jsonHeaders } from './lib/tasks.js';

http.setResponseCallback(http.expectedStatuses(201, 409));

const created = new Counter('tasks_created');
const conflicts = new Counter('tasks_conflict');
const unexpected = new Counter('tasks_unexpected_status');

const concurrentRequests = Number(__ENV.CONCURRENT_REQUESTS || 50);

export const options = {
  scenarios: {
    race: {
      executor: 'shared-iterations',
      vus: concurrentRequests,
      iterations: concurrentRequests,
      maxDuration: '1m',
    },
  },
  thresholds: {
    tasks_created: [`count==${concurrentRequests > 0 ? 1 : 0}`],
    tasks_conflict: [`count==${Math.max(concurrentRequests - 1, 0)}`],
    tasks_unexpected_status: ['count==0'],
    http_req_duration: ['p(95)<3000'],
  },
};

export function setup() {
  return {
    raceTitle: __ENV.RACE_TITLE || `race-${Date.now()}`,
  };
}

export default function (data) {
  const payload = buildTaskPayload(data.raceTitle);
  const response = http.post(TASKS_URL, payload, {
    ...jsonHeaders,
    tags: { name: 'CreateTaskRace' },
  });

  if (response.status === 201) {
    created.add(1);
  } else if (response.status === 409) {
    conflicts.add(1);
  } else {
    unexpected.add(1);
  }

  check(response, {
    'status is 201 or 409': (r) => r.status === 201 || r.status === 409,
  });
}
