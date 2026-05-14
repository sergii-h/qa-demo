# EVENT-001: Task Created Event

**Epic:** Event Streaming  
**Priority:** High  
**Story Points:** 3

## Description
As a system, I want to publish task created events to Kafka so that downstream systems can react to new tasks.

## Acceptance Criteria

1. Should publish event to Kafka topic with eventType "CREATED" when task is successfully created and saved to database

2. Should include taskId, title, status, priority, timestamp, and eventType in published event

3. Should set Kafka message key to task ID for partition ordering

4. Should not publish event when task creation fails validation (HTTP 400)

5. Should not publish event when task creation fails due to duplicate title (HTTP 409)

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should publish event to Kafka topic `task-event` when task created
3. **Pact** - N/A
4. **E2E** - N/A (event streaming not visible in UI)
5. **UAT** - N/A

## Technical Notes
- Kafka topic configured via `kafka.topic.task-event` property
- Event timestamp is current time when event is created
- Uses JSON serialization for event payload
- Task description is not included in event

