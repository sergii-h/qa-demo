# Epic 1: Task Management

CRUD API for tasks with validation, unique titles, and lifecycle timestamps.

## User Stories

- [TASK-001](TASK-001-create-task.md) — Create Task (5 pts)
- [TASK-002](TASK-002-retrieve-task.md) — Retrieve Single Task (2 pts)
- [TASK-003](TASK-003-update-task.md) — Update Task (5 pts)
- [TASK-004](TASK-004-delete-task.md) — Delete Task (3 pts)
- [TASK-005](TASK-005-list-tasks.md) — List All Tasks (2 pts)

**Total:** 17 pts

## Epic scope

- Title required, unique, max 100 chars; description optional, max 500 chars
- Status: TODO, IN_PROGRESS, DONE; priority: LOW, MEDIUM, HIGH
- `createdDate` / `updatedDate` managed by the system
- Endpoints: `POST/GET/PUT/DELETE /v1/tasks`, `GET /v1/tasks/{id}`
- Kafka lifecycle events covered in [Epic 3](../epic-3-event-streaming/README.md)
