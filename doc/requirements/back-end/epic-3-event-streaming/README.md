# Epic 3: Event Streaming

**Goal:** Publish task lifecycle events to enable event-driven architecture.

## Overview
This epic implements event streaming for task lifecycle events (created, updated, deleted) via Kafka. This enables downstream systems to react to task changes in real-time without tight coupling.

## User Stories
- **[EVENT-001](EVENT-001-task-created-event.md)** - Task Created Event (3 pts)
- **[EVENT-002](EVENT-002-task-updated-event.md)** - Task Updated Event (3 pts)
- **[EVENT-003](EVENT-003-task-deleted-event.md)** - Task Deleted Event (3 pts)

**Total Story Points:** 9

## Event Schema
```json
{
  "taskId": "string",
  "title": "string",
  "status": "TODO|IN_PROGRESS|DONE",
  "priority": "LOW|MEDIUM|HIGH",
  "timestamp": "ISO-8601 timestamp",
  "eventType": "CREATED|UPDATED|DELETED"
}
```

## Business Rules
- Events are published only after successful database operations
- Events are not published for validation failures or not found errors
- Message key is set to task ID for partition ordering
- Event timestamp reflects when event was generated, not task updatedDate

## Dependencies
- Kafka message broker
- Configured Kafka topic: `task-event`

## Technical Details
- Uses Spring Kafka with JSON serialization
- Synchronous event publishing (blocks until Kafka confirms)
- No retry logic for failed publishes

