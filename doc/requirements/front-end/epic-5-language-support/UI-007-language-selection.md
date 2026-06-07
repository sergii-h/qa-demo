# UI-007: Language Selection

**Points:** 5 · **Epic:** Language Support

As a user, I want EN/ES with client locale detection and a manual switcher so I can use my preferred language.

## Acceptance Criteria

1. Should default to Spanish on load for `es` / `es-*` client locale; English otherwise
2. Should show `EN` / `ES` language switcher; switch language instantly without full reload
3. Should translate static UI (list, buttons, flows, labels, errors) and status/priority tags and field labels:
   - TODO → To Do / Por hacer; IN_PROGRESS → In Progress / En progreso; DONE → Done / Hecho
   - LOW/MEDIUM/HIGH → Low/Baja, Medium/Media, High/Alta

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should render language switcher with `EN` and `ES` options
   - Should change current language when user selects another language option
   - Per-flow translation coverage: [UI-001](../epic-1-task-display/UI-001-tasks-table.md), [UI-003](../epic-2-task-creation/UI-003-create-task-modal.md), [UI-004](../epic-3-task-editing/UI-004-edit-task-modal.md), [UI-005](../epic-4-task-information/UI-005-info-task-modal.md) IT
3. **Pact** - N/A
4. **E2E**
   - Should switch all UI text and status/priority tag values to Spanish when ES is selected
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual**
   - Visual check — switcher placement, translated UI and tag labels (EN/ES)
   - Keyboard navigation for language selector
