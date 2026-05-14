# Epic 2: Task Creation

**Goal:** Enable users to create new tasks through an intuitive modal interface.

## Overview
This epic focuses on the task creation workflow, providing a user-friendly form within a modal dialog where users can enter all task information with client-side validation before submitting to the backend.

## User Stories
- **[UI-003](UI-003-create-task-modal.md)** - Create Task Modal (5 pts)

**Total Story Points:** 5

## Key Features
- Modal dialog for focused task creation
- Form with title, description, status, and priority fields
- Client-side validation (required fields, max length)
- Default values (status: TODO, priority: MEDIUM)
- Error handling and user-friendly error messages
- Loading state during submission
- Automatic table refresh after creation

## UI Components
- PrimeReact Dialog
- PrimeReact InputText (for title)
- PrimeReact InputTextarea (for description)
- PrimeReact Dropdown (for status and priority)
- PrimeReact ProgressSpinner (for loading)
- PrimeReact Button (for actions)

## Validation Rules
- **Title**: Required, max 100 characters
- **Description**: Optional, max 500 characters (backend enforced)
- **Status**: Required (default TODO)
- **Priority**: Required (default MEDIUM)

## Error Scenarios
- Empty title → "Title is required"
- Title too long → "Title must not exceed 100 characters"
- Duplicate title → "Task with this title already exists"
- General error → "Failed to create task. Please try again."

## Dependencies
- Backend API (`POST /v1/tasks`)
- CreateTaskModal React component
- createTask service function

## User Workflow
```
1. User clicks "Create task" button
2. Modal opens with empty form (default values set)
3. User enters task information
4. Client validates input
5. User clicks "Create"
6. Loading spinner shows
7. Backend creates task
8. Modal closes
9. Table refreshes with new task
```

