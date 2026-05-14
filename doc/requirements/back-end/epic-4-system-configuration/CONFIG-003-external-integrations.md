# CONFIG-003: External Service Integration

**Epic:** System Configuration  
**Priority:** High  
**Story Points:** 3

## Description
As a system, I want to configure connections to external services (MongoDB, Kafka, External API) so that the application can integrate with required dependencies.

## Acceptance Criteria

### MongoDB Integration
1. Should establish connection to MongoDB at configured URI when application starts

2. Should persist data to "tasks" collection in configured database when task operations are performed

### Kafka Integration
3. Should initialize Kafka producer with configured bootstrap servers when application starts

4. Should send messages to configured topic with JSON serialization when events are published

5. Should use String key serializer and JSON value serializer for Kafka producer

### External Service Integration
6. Should create WebClient with base URL set to configured external service URL

7. Should make requests to configured base URL when external validation is called

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should connect to MongoDB at configured URI
   - Should persist data to "tasks" collection
   - Should initialize Kafka producer with configured bootstrap servers
   - Should send events with JSON serialization to configured topic
   - Should create WebClient with configured base URL for external service
3. **Pact** - N/A
4. **E2E** - Implicitly tested (application functions correctly)
5. **UAT** - N/A

## Technical Notes
- MongoDB URI: `spring.data.mongodb.uri` (default: `mongodb://localhost:27018/task_db`)
- Kafka bootstrap servers: `kafka.bootstrap-servers` (default: `localhost:9094`)
- Kafka topic: `kafka.topic.task-event` (default: `task-event`)
- External service: `external.service.url` (default: `http://localhost:8085`)
- All configurations support environment-specific overrides

