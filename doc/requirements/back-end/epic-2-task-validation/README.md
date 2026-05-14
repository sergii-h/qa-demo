# Epic 2: Task Validation

**Goal:** Enable external validation of tasks against business rules.

## Overview
This epic provides integration with an external validation service that can verify task data against external business rules. This allows decoupling of validation logic from the core service.

## User Stories
- **[VALID-001](VALID-001-external-validation.md)** - External Task Validation (3 pts)

**Total Story Points:** 3

## Business Rules
- Validation does not modify task data
- Non-existent tasks return false (not 404)
- Validation uses reactive/non-blocking communication

## Dependencies
- External validation service running at configured URL
- WebClient for reactive HTTP communication

## API Endpoints
```
GET /v1/tasks/isValid/{id} - Validate task against external rules
```

