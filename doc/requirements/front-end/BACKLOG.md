# Task Management UI - Product Backlog

## Project Overview
Frontend React application for task management with modal-based CRUD operations, built with React 18, TypeScript, and PrimeReact.

## Epic Summary

| Epic | Description | Stories | Total Points |
|------|-------------|---------|--------------|
| [Epic 1: Task Display](epic-1-task-display/README.md) | Task table and deletion | 2 | 5 |
| [Epic 2: Task Creation](epic-2-task-creation/README.md) | Create task modal | 1 | 5 |
| [Epic 3: Task Editing](epic-3-task-editing/README.md) | Edit task modal | 1 | 5 |
| [Epic 4: Task Information](epic-4-task-information/README.md) | Info modal and validation | 2 | 5 |
| [Epic 5: Language Support](epic-5-language-support/README.md) | Internationalisation | 1 | 5 |
| **TOTAL** | | **7** | **25** |

## All User Stories

### Epic 1: Task Display (5 pts)
- ✅ **UI-001** - Tasks Table Display (3 pts)
  - Display all tasks in data table with visual indicators
- ✅ **UI-002** - Delete Task (2 pts)
  - Delete task from table with optimistic UI update

### Epic 2: Task Creation (5 pts)
- ✅ **UI-003** - Create Task Modal (5 pts)
  - Create new task through modal form with validation

### Epic 3: Task Editing (5 pts)
- ✅ **UI-004** - Edit Task Modal (5 pts)
  - Edit existing task with pre-populated form

### Epic 4: Task Information (5 pts)
- ✅ **UI-005** - Info Task Modal (3 pts)
  - View read-only task details
- ✅ **UI-006** - External Validation Display (2 pts)
  - Show external validation status

### Epic 5: Language Support (5 pts)
- ✅ **UI-007** - Language Selection (5 pts)
  - Browser language detection with manual switcher, full UI translation (EN/ES)

## UI Components Overview

### Main Application
```
<App>
  └─ <TasksTable>
      ├─ <LanguageSwitcher>
      ├─ <DataTable> (PrimeReact)
      ├─ <InfoTaskModal> (conditional)
      ├─ <EditTaskModal> (conditional)
      └─ <CreateTaskModal> (conditional)
```

### Component Breakdown

**TasksTable** (UI-001, UI-002)
- Displays all tasks in table format
- Handles task deletion
- Manages modal visibility state

**CreateTaskModal** (UI-003)
- Title input (required)
- Description textarea (optional)
- Status dropdown (default: TODO)
- Priority dropdown (default: MEDIUM)
- Client-side validation
- Error handling

**EditTaskModal** (UI-004)
- Same fields as create modal
- Pre-populated with existing data
- Loading state for data fetch

**InfoTaskModal** (UI-005, UI-006)
- Read-only task display
- Formatted timestamps
- External validation indicator

## Visual Design Patterns

### Status Colors
- **TODO**: Blue (info)
- **IN_PROGRESS**: Orange (warning)
- **DONE**: Green (success)

### Priority Colors
- **LOW**: Green (success)
- **MEDIUM**: Orange (warning)
- **HIGH**: Red (danger)

### Button Styles
- **Create**: Info button with plus icon
- **Info**: Outlined button with info-circle icon
- **Edit**: Outlined button with pencil icon
- **Delete**: Outlined danger button with trash icon

## Form Validation Rules

### Title Field
- Required: ✅
- Max length: 100 characters
- Error messages:
  - "Title is required"
  - "Title must not exceed 100 characters"
  - "Task with this title already exists" (duplicate)

### Description Field
- Required: ❌
- Max length: 500 characters (backend enforced)

### Status Field
- Required: ✅
- Options: TODO, IN_PROGRESS, DONE
- Default: TODO

### Priority Field
- Required: ✅
- Options: LOW, MEDIUM, HIGH
- Default: MEDIUM

## API Integration

### Backend Endpoints Used
```
GET    /v1/tasks              → Fetch all tasks
GET    /v1/tasks/{id}         → Fetch single task
POST   /v1/tasks              → Create task
PUT    /v1/tasks/{id}         → Update task
DELETE /v1/tasks/{id}         → Delete task
GET    /v1/tasks/isValid/{id} → Validate task
```

### Service Functions
```typescript
getTasks(): Promise<ITask[]>
getTask(taskId: string): Promise<ITask>
createTask(task: ITask): Promise<ITask>
updateTask(task: ITask): Promise<ITask>
deleteTask(taskId: string): Promise<any>
getIsValid(taskId: string): Promise<boolean>
```

## Data Model

```typescript
interface ITask {
  id?: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  createdDate?: string;
  updatedDate?: string;
}

enum TaskStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE'
}

enum TaskPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH'
}
```

## Technology Stack

### Core
- **Framework**: React 18
- **Language**: TypeScript
- **UI Library**: PrimeReact (DataTable, Dialog, Button, Tag, Dropdown, InputText, InputTextarea, ProgressSpinner)
- **Icons**: PrimeIcons
- **Build Tool**: npm, React Scripts

### Testing
- **Framework**: Vitest
- **Component Testing**: React Testing Library
- **Contract Testing**: Pact

## CSS Classes Reference

### Component Classes
- `tasks-table` - Main data table
- `task-info-button` - Info button
- `edit-task-button` - Edit button
- `delete-task-button` - Delete button
- `add-task-button` - Create task button
- `create-button` - Create modal submit button
- `save-button` - Edit modal submit button
- `close-button` - Modal close button

### Modal IDs
- `create-task-modal` - Create modal
- `edit-task-modal` - Edit modal
- `info-task-modal` - Info modal

### Test IDs
- `create-task-title-input` - Title input in create modal
- `create-button` - Create button
- `close-button` - Close button
- `title-error` - Title error message
- `loading-spinner` - Loading indicator
- `valid` - Valid icon
- `notValid` - Invalid icon

## User Workflows

### Create Task Flow
```
1. Click "Create task" button
2. Modal opens with empty form
3. Fill in title (required)
4. Fill in description (optional)
5. Select status (default: TODO)
6. Select priority (default: MEDIUM)
7. Click "Create"
8. Validation runs
9. If valid → POST to backend
10. On success → Modal closes, table refreshes
11. On error → Show error message
```

### Edit Task Flow
```
1. Click "Edit" on task row
2. Modal opens with spinner
3. Fetch task data
4. Form populates
5. Modify fields
6. Click "Save"
7. Validation runs
8. If valid → PUT to backend
9. On success → Modal closes, table refreshes
10. On error → Show error message
```

### Delete Task Flow
```
1. Click "Delete" on task row
2. DELETE request sent
3. Task removed from local state
4. Table updates immediately
```

### View Task Info Flow
```
1. Click "Info" on task row
2. Modal opens with spinner
3. Fetch task data
4. Fetch validation status
5. Display all information
6. User reviews and closes
```

## Development Status
All stories reflect **implemented functionality** based on existing codebase analysis. This backlog serves as documentation of current UI capabilities.

