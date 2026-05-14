# Quick Reference - Task Management UI

## 📁 Structure Overview
```
requirements-fe/
├── BACKLOG.md                      # Complete backlog
├── QUICK-REFERENCE.md              # This file
├── README.md                       # Navigation guide
├── epic-1-task-display/            # 2 stories, 5 pts
│   ├── UI-001-tasks-table.md
│   └── UI-002-delete-task.md
├── epic-2-task-creation/           # 1 story, 5 pts
│   └── UI-003-create-task-modal.md
├── epic-3-task-editing/            # 1 story, 5 pts
│   └── UI-004-edit-task-modal.md
├── epic-4-task-information/        # 2 stories, 5 pts
│   ├── UI-005-info-task-modal.md
│   └── UI-006-external-validation.md
└── epic-5-language-support/        # 1 story, 5 pts
    └── UI-007-language-selection.md
```

## 📊 Quick Stats
- **Total Stories**: 7 user stories
- **Total Story Points**: 25 points
- **Total Epics**: 5

## 🎯 Epic Breakdown

| Epic | Focus | Stories | Points |
|------|-------|---------|--------|
| **1. Task Display** | Table & deletion | 2 | 5 |
| **2. Task Creation** | Create modal | 1 | 5 |
| **3. Task Editing** | Edit modal | 1 | 5 |
| **4. Task Information** | Info modal | 2 | 5 |
| **5. Language Support** | i18n & switcher | 1 | 5 |

## 🎨 UI Components

### PrimeReact Components Used
- **DataTable** - Tasks table display
- **Dialog** - All modals
- **Button** - All actions
- **Tag** - Status and priority indicators
- **InputText** - Title input fields
- **InputTextarea** - Description fields
- **Dropdown** - Status and priority selectors, language switcher
- **ProgressSpinner** - Loading states

### Icon Set
- **pi-plus** - Create button
- **pi-info-circle** - Info button
- **pi-pencil** - Edit button
- **pi-trash** - Delete button
- **pi-check** - Validation success
- **pi-times** - Validation failure, close button

## 🎨 Visual Design

### Status Colors
```
TODO          → Blue (info severity)
IN_PROGRESS   → Orange (warning severity)
DONE          → Green (success severity)
```

### Priority Colors
```
LOW     → Green (success severity)
MEDIUM  → Orange (warning severity)
HIGH    → Red (danger severity)
```

### Button Styles
```
Create  → p-button-info (blue)
Info    → p-button-outlined (neutral)
Edit    → p-button-outlined (neutral)
Delete  → p-button-outlined p-button-danger (red)
```

## 📋 Component Reference

### LanguageSwitcher Component
**File**: `src/components/languageSwitcher/languageSwitcher.tsx`

**Features**:
- Dropdown in top-right corner of page
- Detects browser language automatically on load
- Allows manual override (EN / ES)
- Triggers full re-render of all translated content on switch

**State**:
- Reads `i18n.resolvedLanguage` from `useTranslation` hook

### TasksTable Component
**File**: `src/components/tasksTable/tasksTable.tsx`

**Features**:
- Fetches and displays all tasks
- Striped rows for readability
- Status/priority as colored tags
- Action buttons (Info, Edit, Delete)
- Create task button

**State**:
- `tasks[]` - Array of tasks
- `activeTaskId` - Currently selected task ID
- `isInfoModalOpen` - Info modal visibility
- `isEditModalOpen` - Edit modal visibility
- `isCreateModalOpen` - Create modal visibility

**Functions**:
- `fetchTasks()` - Load tasks from backend
- `deleteTaskById(taskId)` - Delete and update table

### CreateTaskModal Component
**File**: `src/components/createTaskModal/createTaskModal.tsx`

**Demo Scope Note**:
- Input normalization (for example trimming title/description on submit) is intentionally out of scope.

**Props**:
- `onClose: () => void` - Close callback
- `onSave: () => void` - Save success callback
- `isLoading?: boolean` - Loading state

**State**:
- `title` - Task title
- `description` - Task description
- `status` - Selected status (default: TODO)
- `priority` - Selected priority (default: MEDIUM)
- `titleError` - Validation error message

**Validation**:
- Title required
- Title max 100 chars
- Duplicate title detection

### EditTaskModal Component
**File**: `src/components/editTaskModal/editTaskModal.tsx`

**Props**:
- `onClose: () => void` - Close callback
- `onSave: () => void` - Save success callback
- `taskId: string` - Task to edit

**State**:
- `isLoading` - Data fetch/save loading
- `task` - Loaded task object
- `title, description, status, priority` - Form fields
- `titleError` - Validation error message

**Lifecycle**:
1. Fetch task on mount
2. Populate form
3. User edits
4. Validate and save

### InfoTaskModal Component
**File**: `src/components/infoTaskModal/infoTaskModal.tsx`

**Props**:
- `onClose: () => void` - Close callback
- `taskId: string` - Task to display

**State**:
- `isLoading` - Data fetch loading
- `task` - Loaded task object
- `valid` - External validation result

**Displays**:
- Title (in header)
- Description
- Status (tag)
- Priority (tag)
- Created date
- Updated date
- Validation status (icon)

## 📡 API Integration

### Service Functions
```typescript
// Located in src/services/index.ts
getTasks(): Promise<ITask[]>
getTask(taskId: string): Promise<ITask>
getIsValid(taskId: string): Promise<boolean>
createTask(task: ITask): Promise<ITask>
updateTask(task: ITask): Promise<ITask>
deleteTask(taskId: string): Promise<any>
```

### Environment
```
REACT_APP_BE_API = http://localhost:8080/v1
```

## 📝 Data Interface

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

## ✅ Validation Rules

### Title Field
- **Required**: Yes
- **Max Length**: 100 characters
- **Errors**:
  - Empty: "Title is required"
  - Too long: "Title must not exceed 100 characters"
  - Duplicate: "Task with this title already exists"

### Description Field
- **Required**: No
- **Max Length**: 500 characters (backend)

### Status Field
- **Required**: Yes
- **Options**: TODO, IN_PROGRESS, DONE
- **Default**: TODO

### Priority Field
- **Required**: Yes
- **Options**: LOW, MEDIUM, HIGH
- **Default**: MEDIUM

## 🧪 Test Identifiers

### Data Test IDs
```typescript
language-switcher        // Language selector dropdown
create-task-title-input  // Title input in create modal
create-button            // Create button
close-button             // Close button
title-error              // Error message
loading-spinner          // Loading indicator
valid                    // Validation success icon
notValid                 // Validation failure icon
```

### Element IDs
```typescript
create-task-modal   // Create modal
edit-task-modal     // Edit modal
info-task-modal     // Info modal
description         // Description text
status              // Status tag
priority            // Priority tag
createdDate         // Created timestamp
updatedDate         // Updated timestamp
valid               // Validation label
```

### CSS Classes
```typescript
tasks-table          // Main table
task-info-button     // Info button
edit-task-button     // Edit button
delete-task-button   // Delete button
add-task-button      // Create button
create-button        // Create submit
save-button          // Edit submit
close-button         // Close button
```

## 🔄 User Flows

### Create Task
```
Button → Modal → Form → Validate → API → Refresh → Close
```

### Edit Task
```
Button → Modal → Load → Form → Validate → API → Refresh → Close
```

### Delete Task
```
Button → API → Remove from State → Update Table
```

### View Info
```
Button → Modal → Load Data → Load Validation → Display
```

## 🎨 Modal Specifications

All modals:
- **Min Width**: 480px
- **Close**: Click outside or close button
- **Footer**: Action buttons (right-aligned)
- **Loading**: ProgressSpinner during async operations

## 🚀 Where to Start

| I Need... | Go To... |
|-----------|----------|
| All UI stories | `BACKLOG.md` |
| Component details | Individual story files |
| Quick component ref | This file |
| Epic overviews | Epic README files |
| Validation rules | This file or BACKLOG.md |
| UI patterns | This file (Visual Design section) |

---

**Tech Stack**: React 18 + TypeScript + PrimeReact  
**Components**: 5 main components (Table + 3 modals + language switcher)  
**API**: REST calls to Spring Boot backend  
**i18n**: i18next + react-i18next, EN/ES locales  
**Last Updated**: 2026-04-04

