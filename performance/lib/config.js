export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const TASKS_URL = `${BASE_URL}/v1/tasks`;

export const readThresholds = {
  http_req_duration: ['p(95)<500'],
  http_req_failed: ['rate<0.01'],
};

export const writeThresholds = {
  http_req_duration: ['p(95)<2000'],
  http_req_failed: ['rate<0.01'],
};
