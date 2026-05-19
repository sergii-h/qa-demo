# Task Management Service - Requirements

| Role | Start Here | Purpose |
|------|------------|---------|
| Product Owner / QA | [BACKLOG.md](BACKLOG.md) | All stories, data model, API reference |
| Developer / QA | Epic folders below | Detailed acceptance criteria |

## Epics

### [Epic 1: Task Management](epic-1-task-management/README.md)
CRUD operations for tasks with business rules and validation.

- [TASK-001](epic-1-task-management/TASK-001-create-task.md) - Create Task
- [TASK-002](epic-1-task-management/TASK-002-retrieve-task.md) - Retrieve Single Task
- [TASK-003](epic-1-task-management/TASK-003-update-task.md) - Update Task
- [TASK-004](epic-1-task-management/TASK-004-delete-task.md) - Delete Task
- [TASK-005](epic-1-task-management/TASK-005-list-tasks.md) - List All Tasks

### [Epic 2: Task Validation](epic-2-task-validation/README.md)
Integration with external validation service.

- [VALID-001](epic-2-task-validation/VALID-001-external-validation.md) - External Task Validation

### [Epic 3: Event Streaming](epic-3-event-streaming/README.md)
Kafka-based event publishing for task lifecycle events.

- [EVENT-001](epic-3-event-streaming/EVENT-001-task-created-event.md) - Task Created Event
- [EVENT-002](epic-3-event-streaming/EVENT-002-task-updated-event.md) - Task Updated Event
- [EVENT-003](epic-3-event-streaming/EVENT-003-task-deleted-event.md) - Task Deleted Event

### [Epic 4: System Configuration](epic-4-system-configuration/README.md)
Cross-cutting concerns: CORS, error handling, external service configuration.

- [CONFIG-001](epic-4-system-configuration/CONFIG-001-cors-configuration.md) - CORS Configuration
- [CONFIG-002](epic-4-system-configuration/CONFIG-002-error-handling.md) - Error Handling
- [CONFIG-003](epic-4-system-configuration/CONFIG-003-external-integrations.md) - External Service Integration
