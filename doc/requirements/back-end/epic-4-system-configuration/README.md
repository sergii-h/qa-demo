# Epic 4: System Configuration

Cross-cutting API setup: CORS, global error handling, and external service wiring.

## User Stories

- [CONFIG-001](CONFIG-001-cors-configuration.md) — CORS Configuration (2 pts)
- [CONFIG-002](CONFIG-002-error-handling.md) — Error Handling (3 pts)
- [CONFIG-003](CONFIG-003-external-integrations.md) — External Service Integration (3 pts)

**Total:** 8 pts

## Epic scope

- CORS enabled for frontend integration (restrict origins in production)
- Consistent errors: 400 field map, 404 `message`, 409 duplicate title
- MongoDB, Kafka, and external validation URL configured via application properties
