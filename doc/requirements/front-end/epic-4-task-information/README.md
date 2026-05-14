# Epic 4: Task Information

**Goal:** Provide detailed read-only view of task information including external validation status.

## Overview
This epic presents task details in an information-only modal, showing all task properties including created/updated timestamps and external validation status from the backend service.

## User Stories
- **[UI-005](UI-005-info-task-modal.md)** - Info Task Modal (3 pts)
- **[UI-006](UI-006-external-validation.md)** - External Validation Display (2 pts)

**Total Story Points:** 5

## Key Features
- Read-only modal display
- All task properties visible
- Formatted timestamps (locale-specific)
- Visual status and priority indicators
- External validation status with icon
- Loading state while fetching data

## UI Components
- PrimeReact Dialog
- PrimeReact Tag (for status and priority)
- PrimeReact ProgressSpinner (for loading)
- PrimeIcons (for validation status)

## Information Displayed
- **Title**: Modal header
- **Description**: Full text or "No description"
- **Status**: Colored tag (TODO/IN_PROGRESS/DONE)
- **Priority**: Colored tag (LOW/MEDIUM/HIGH)
- **Created Date**: Formatted timestamp
- **Last Updated**: Formatted timestamp
- **Validated**: Green check or red X icon

## Dependencies
- Backend API (`GET /v1/tasks/{id}`, `GET /v1/tasks/isValid/{id}`)
- InfoTaskModal React component
- getTask and getIsValid service functions

## User Workflow
```
1. User clicks "Info" button on task row
2. Modal opens with loading spinner
3. Task data is fetched from backend
4. Validation status is fetched from backend
5. Modal displays all information in read-only format
6. User reviews task details
7. User closes modal
```

## External Validation
The validation indicator shows whether the task passes external business rules validation:
- **Green check**: Task is valid
- **Red X**: Task is not valid

This provides users with feedback about task data quality without blocking task operations.

