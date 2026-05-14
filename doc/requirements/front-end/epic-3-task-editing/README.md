# Epic 3: Task Editing

**Goal:** Allow users to update existing task information through a pre-populated form.

## Overview
This epic enables users to modify task details through an edit modal that loads existing task data and allows changes with the same validation rules as task creation.

## User Stories
- **[UI-004](UI-004-edit-task-modal.md)** - Edit Task Modal (5 pts)

**Total Story Points:** 5

## Key Features
- Modal dialog with task title in header
- Form pre-populated with existing task data
- Loading state while fetching task data
- Client-side validation (required fields, max length)
- Error handling and user-friendly error messages
- Loading state during update
- Automatic table refresh after update

## UI Components
- PrimeReact Dialog
- PrimeReact InputText (for title)
- PrimeReact InputTextarea (for description)
- PrimeReact Dropdown (for status and priority)
- PrimeReact ProgressSpinner (for loading)
- PrimeReact Button (for actions)

## Validation Rules
Same as create modal:
- **Title**: Required, max 100 characters
- **Description**: Optional
- **Status**: Required
- **Priority**: Required

## Error Scenarios
- Empty title → "Title is required"
- Title too long → "Title must not exceed 100 characters"
- Duplicate title → "Task with this title already exists"
- General error → "Failed to update task. Please try again."

## Dependencies
- Backend API (`GET /v1/tasks/{id}`, `PUT /v1/tasks/{id}`)
- EditTaskModal React component
- getTask and updateTask service functions

## User Workflow
```
1. User clicks "Edit" button on task row
2. Modal opens with loading spinner
3. Task data is fetched from backend
4. Form populates with current values
5. User modifies fields
6. Client validates input
7. User clicks "Save"
8. Loading spinner shows
9. Backend updates task
10. Modal closes
11. Table refreshes with updated task
```

