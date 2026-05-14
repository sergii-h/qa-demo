# EVENT-002: Task Updated Event

**Epic:** Event Streaming  
**Priority:** High  
**Story Points:** 3

## Description
As a system, I want to publish task updated events to Kafka so that downstream systems can react to task changes.

## Acceptance Criteria

1. Should publish event to Kafka topic with eventType "UPDATED" when task is successfully updated and saved to database

2. Should include taskId, title, status, priority, timestamp, and eventType in published event

3. Should set Kafka message key to task ID for partition ordering

4. Should contain updated task values in event payload

5. Should not publish event when update fails validation (HTTP 400)

6. Should not publish event when task not found (HTTP 404)

7. Should not publish event when update fails due to duplicate title (HTTP 409)

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should publish event to Kafka topic `task-event` when task updated
3. **Pact** - N/A
4. **E2E** - N/A (event streaming not visible in UI)
5. **UAT** - N/A

## Technical Notes
- Kafka topic configured via `kafka.topic.task-event` property
- Event timestamp is current time when event is created
- Uses JSON serialization for event payload
- Event contains updated task values

