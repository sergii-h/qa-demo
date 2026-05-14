# Task Management UI - Frontend Requirements

This directory contains comprehensive requirements documentation for the Task Management UI frontend application with **9 user stories** across **5 epics**.

## 🚀 Quick Start

Choose your entry point:

| Role | Start Here | Purpose |
|------|------------|---------|
| 👨‍💼 Product Owner | [BACKLOG.md](BACKLOG.md) | Complete backlog, sprint planning |
| 👨‍💻 Developer | [Epic Folders](#epics) | Implementation details & AC |
| 🧪 QA Engineer | [Epic Folders](#epics) | Test scenarios & acceptance criteria |
| ⚡ Quick Lookup | [QUICK-REFERENCE.md](QUICK-REFERENCE.md) | UI components & features |

## 📚 Documentation Files

- **[BACKLOG.md](BACKLOG.md)** - Complete product backlog with all 9 stories
- **[QUICK-REFERENCE.md](QUICK-REFERENCE.md)** - Fast UI component reference

## 📦 Epics

### [Epic 1: Task Display](epic-1-task-display/README.md) - TBD pts
Display and manage tasks in a data table with visual status indicators.

- [UI-001](epic-1-task-display/UI-001-tasks-table.md) - Tasks Table Display
- [UI-002](epic-1-task-display/UI-002-delete-task.md) - Delete Task

### [Epic 2: Task Creation](epic-2-task-creation/README.md) - TBD pts
Create new tasks through a modal dialog with form validation.

- [UI-003](epic-2-task-creation/UI-003-create-task-modal.md) - Create Task Modal

### [Epic 3: Task Editing](epic-3-task-editing/README.md) - TBD pts
Edit existing tasks with pre-populated form data.

- [UI-004](epic-3-task-editing/UI-004-edit-task-modal.md) - Edit Task Modal

### [Epic 4: Task Information](epic-4-task-information/README.md) - TBD pts
View detailed task information including external validation status.

- [UI-005](epic-4-task-information/UI-005-info-task-modal.md) - Info Task Modal
- [UI-006](epic-4-task-information/UI-006-external-validation.md) - External Validation Display

### [Epic 5: Language Support](epic-5-language-support/README.md) - TBD pts
Internationalisation with automatic browser language detection and a manual language switcher.

- [UI-007](epic-5-language-support/UI-007-language-selection.md) - Language Selection

## 📁 Directory Structure

```
requirements-fe/
├── BACKLOG.md                    # Complete product backlog
├── QUICK-REFERENCE.md            # UI component quick reference
├── README.md                     # This file
│
├── epic-1-task-display/          # Task table and deletion
│   ├── README.md
│   ├── UI-001-tasks-table.md
│   └── UI-002-delete-task.md
│
├── epic-2-task-creation/         # Task creation workflow
│   ├── README.md
│   └── UI-003-create-task-modal.md
│
├── epic-3-task-editing/          # Task editing workflow
│   ├── README.md
│   └── UI-004-edit-task-modal.md
│
├── epic-4-task-information/      # Task details and validation
│   ├── README.md
│   ├── UI-005-info-task-modal.md
│   └── UI-006-external-validation.md
│
└── epic-5-language-support/      # Internationalisation
    ├── README.md
    └── UI-007-language-selection.md
```

## 🎯 Features

✅ **JIRA-Compatible Format** - Copy directly to your project management tool  
✅ **Hybrid AC Format** - Given-When-Then + "Should..." statements  
✅ **Story Point Estimates** - Sprint planning ready  
✅ **UI Component Details** - PrimeReact components documented  
✅ **User Interactions** - Complete flow documentation  
✅ **Validation Rules** - Frontend validation specified  

## 📊 Statistics

- **Total Stories**: 9 user stories
- **Total Epics**: 5 feature epics
- **Technology**: React 18, TypeScript, PrimeReact
- **Format**: JIRA-style tickets with hybrid acceptance criteria

## 🏷️ Ticket Numbering Convention

- **UI-xxx**: Frontend UI features and components

