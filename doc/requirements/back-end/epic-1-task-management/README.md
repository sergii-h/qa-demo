# Epic 1: Task Management

**Goal:** Provide comprehensive CRUD operations for task management with business rule validation.

## Overview
This epic covers the core functionality of the Task Management Service, enabling users to create, read, update, delete, and list tasks. Each task contains title, description, status, and priority fields with comprehensive validation.

## User Stories
- **[TASK-001](TASK-001-create-task.md)** - Create Task (5 pts)
- **[TASK-002](TASK-002-retrieve-task.md)** - Retrieve Single Task (2 pts)
- **[TASK-003](TASK-003-update-task.md)** - Update Task (5 pts)
- **[TASK-004](TASK-004-delete-task.md)** - Delete Task (3 pts)
- **[TASK-005](TASK-005-list-tasks.md)** - List All Tasks (2 pts)

**Total Story Points:** 17

## Business Rules
- Task titles must be unique across the system
- Title is required and limited to 100 characters
- Description is optional and limited to 500 characters
- Status must be one of: TODO, IN_PROGRESS, DONE
- Priority must be one of: LOW, MEDIUM, HIGH
- System automatically manages createdDate and updatedDate timestamps

## Dependencies
- MongoDB for data persistence
- Kafka event streaming (Epic 3) for lifecycle events

## API Endpoints
```
POST   /v1/tasks          - Create task
GET    /v1/tasks/{id}     - Get single task
PUT    /v1/tasks/{id}     - Update task
DELETE /v1/tasks/{id}     - Delete task
GET    /v1/tasks          - List all tasks
```

