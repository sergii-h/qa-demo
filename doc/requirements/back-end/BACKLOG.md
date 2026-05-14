# Task Management Service - Product Backlog

## Project Overview
Backend REST API service for task management with event streaming capabilities, built with Spring Boot, MongoDB, and Kafka.

## Epic Summary

| Epic | Description | Stories | Total Points |
|------|-------------|---------|--------------|
| [Epic 1: Task Management](epic-1-task-management/README.md) | Core CRUD operations for tasks | 5 | 17 |
| [Epic 2: Task Validation](epic-2-task-validation/README.md) | External task validation | 1 | 3 |
| [Epic 3: Event Streaming](epic-3-event-streaming/README.md) | Kafka event publishing | 3 | 9 |
| [Epic 4: System Configuration](epic-4-system-configuration/README.md) | Cross-cutting concerns | 3 | 8 |
| **TOTAL** | | **12** | **37** |

## All User Stories

### Epic 1: Task Management (17 pts)
- ✅ **TASK-001** - Create Task (5 pts)
  - Create tasks with validation and duplicate title checking
- ✅ **TASK-002** - Retrieve Single Task (2 pts)
  - Get task by ID with 404 handling
- ✅ **TASK-003** - Update Task (5 pts)
  - Update tasks with validation and duplicate checking
- ✅ **TASK-004** - Delete Task (3 pts)
  - Delete tasks with 404 handling
- ✅ **TASK-005** - List All Tasks (2 pts)
  - Retrieve all tasks

### Epic 2: Task Validation (3 pts)
- ✅ **VALID-001** - External Task Validation (3 pts)
  - Validate tasks via external service

### Epic 3: Event Streaming (9 pts)
- ✅ **EVENT-001** - Task Created Event (3 pts)
  - Publish Kafka event on task creation
- ✅ **EVENT-002** - Task Updated Event (3 pts)
  - Publish Kafka event on task update
- ✅ **EVENT-003** - Task Deleted Event (3 pts)
  - Publish Kafka event on task deletion

### Epic 4: System Configuration (8 pts)
- ✅ **CONFIG-001** - CORS Configuration (2 pts)
  - Enable CORS for frontend integration
- ✅ **CONFIG-002** - Error Handling (3 pts)
  - Global exception handling with consistent error responses
- ✅ **CONFIG-003** - External Service Integration (3 pts)
  - Configure MongoDB, Kafka, and external API connections

## Data Model

### Task Entity
```json
{
  "id": "string (MongoDB ObjectId)",
  "title": "string (required, max 100 chars, unique)",
  "description": "string (optional, max 500 chars)",
  "status": "TODO | IN_PROGRESS | DONE (required)",
  "priority": "LOW | MEDIUM | HIGH (required)",
  "createdDate": "ISO-8601 timestamp",
  "updatedDate": "ISO-8601 timestamp"
}
```

### Task Event
```json
{
  "taskId": "string",
  "title": "string",
  "status": "TODO | IN_PROGRESS | DONE",
  "priority": "LOW | MEDIUM | HIGH",
  "timestamp": "ISO-8601 timestamp",
  "eventType": "CREATED | UPDATED | DELETED"
}
```

## API Endpoints

### Task Management
- `POST /v1/tasks` - Create task (201, 400, 409)
- `GET /v1/tasks/{id}` - Get task (200, 404)
- `PUT /v1/tasks/{id}` - Update task (200, 400, 404, 409)
- `DELETE /v1/tasks/{id}` - Delete task (204, 404)
- `GET /v1/tasks` - List all tasks (200)

### Task Validation
- `GET /v1/tasks/isValid/{id}` - Validate task (200)

## HTTP Status Codes

| Code | Usage |
|------|-------|
| 200 | Successful GET/validation |
| 201 | Successful POST (creation) |
| 204 | Successful DELETE |
| 400 | Validation errors |
| 404 | Resource not found |
| 409 | Duplicate title conflict |

## Technology Stack
- **Framework**: Spring Boot
- **Database**: MongoDB
- **Messaging**: Kafka
- **External Communication**: WebClient (reactive)
- **Validation**: Jakarta Validation
- **Build Tool**: Maven

## External Dependencies
1. MongoDB server (default: `localhost:27018`)
2. Kafka broker (default: `localhost:9094`)
3. External validation service (default: `http://localhost:8085`)

## Configuration Properties
```properties
spring.data.mongodb.uri=mongodb://localhost:27018/task_db
kafka.bootstrap-servers=localhost:9094
kafka.topic.task-event=task-event
external.service.url=http://localhost:8085
```

## Development Status
All stories reflect **implemented functionality** based on existing codebase analysis. This backlog serves as documentation of current system capabilities.

