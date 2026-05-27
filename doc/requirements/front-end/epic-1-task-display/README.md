# Epic 1: Task Display

Main task table with status/priority tags, row actions, and create entry point.

## User Stories

- [UI-001](UI-001-tasks-table.md) — Tasks Table Display (3 pts)
- [UI-002](UI-002-delete-task.md) — Delete Task (2 pts)

**Total:** 5 pts

## Epic scope

- Table loads via `GET /v1/tasks`; delete removes row only after successful `DELETE`
- Row actions open modals in other epics (create → Epic 2, edit → Epic 3, info → Epic 4)
