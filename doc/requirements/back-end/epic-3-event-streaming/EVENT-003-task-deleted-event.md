# EVENT-003: Task Deleted Event

**Epic:** Event Streaming  
**Priority:** Medium  
**Story Points:** 3

## Description
As a system, I want to publish task deleted events to Kafka so that downstream systems can react to task removal.

## Acceptance Criteria

1. Should publish event to Kafka topic with eventType "DELETED" when task is successfully deleted and removed from database

2. Should include taskId, title, status, priority, timestamp, and eventType in published event

3. Should set Kafka message key to task ID for partition ordering

4. Should not publish event when deletion fails due to task not found (HTTP 404)

5. Should contain task values from before deletion in event payload

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should publish event to Kafka topic `task-event` when task deleted
3. **Pact** - N/A
4. **E2E** - N/A (event streaming not visible in UI)
5. **UAT** - N/A

## Technical Notes
- Kafka topic configured via `kafka.topic.task-event` property
- Event timestamp is current time when event is created
- Uses JSON serialization for event payload
- Event must be published before task data is lost

