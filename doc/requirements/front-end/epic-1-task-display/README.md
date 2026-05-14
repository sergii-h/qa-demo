# Epic 1: Task Display

**Goal:** Display tasks in an organized table with visual indicators and action buttons.

## Overview
This epic covers the main task list view where users can see all their tasks, understand their status at a glance through visual indicators, and take actions like viewing details, editing, or deleting tasks.

## User Stories
- **[UI-001](UI-001-tasks-table.md)** - Tasks Table Display (3 pts)
- **[UI-002](UI-002-delete-task.md)** - Delete Task (2 pts)

**Total Story Points:** 5

## Key Features
- Data table with striped rows for readability
- Visual status indicators with colored tags (TODO/IN_PROGRESS/DONE)
- Visual priority indicators with colored tags (LOW/MEDIUM/HIGH)
- Action buttons for each task (Info, Edit, Delete)
- Create task button for adding new tasks
- Responsive design
- Resilient delete flow with failure handling and retry support

## UI Components
- PrimeReact DataTable
- PrimeReact Tag (for status and priority)
- PrimeReact Button (for actions)

## Dependencies
- Backend API for task data (`GET /v1/tasks`, `DELETE /v1/tasks/{id}`)
- TasksTable React component

## User Workflow
```
1. User opens application
2. Tasks table loads with all tasks
3. User can:
   - Click Info → Open info modal (UI-005)
   - Click Edit → Open edit modal (UI-004)
   - Click Delete → Send delete request and remove row after successful response
   - If delete fails (HTTP/network) → Keep row and allow retry
   - Click Create → Open create modal (UI-003)
```

