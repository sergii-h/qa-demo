# Epic 2: Task Validation

External validation of existing tasks via a reactive HTTP client — read-only, no task mutation.

## User Stories

- [VALID-001](VALID-001-external-validation.md) — External Task Validation (3 pts)

**Total:** 3 pts

## Epic scope

- `GET /v1/tasks/isValid/{id}` returns boolean; missing task returns `false` (not 404)
- Calls external service at `POST /external/validate/task` with title, description, status, priority
