export const jsonHeaders = {
  headers: { 'Content-Type': 'application/json' },
};

export function buildTaskPayload(title, overrides = {}) {
  return JSON.stringify({
    title,
    description: 'Performance test task',
    status: 'TODO',
    priority: 'MEDIUM',
    ...overrides,
  });
}

export function uniqueTitle(prefix) {
  return `${prefix}-${__VU}-${__ITER}-${Date.now()}`;
}
