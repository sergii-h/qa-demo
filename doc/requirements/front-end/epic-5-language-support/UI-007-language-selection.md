# UI-007: Language Selection

**Epic:** Language Support  
**Priority:** Medium  
**Story Points:** 5

## Description
As a user, I want to switch the application language so that I can use the task manager in my preferred language, with all UI text, labels, and status values displayed accordingly.

## Acceptance Criteria

1. Should display application in Spanish automatically on load when browser language is Spanish (`es` or any `es-*` locale)

2. Should default application language to English on load when browser language is not Spanish or is unsupported

3. Should show a language selector dropdown with current language in the top-right corner

4. Should show available language options `EN` and `ES` when language selector is opened

5. Should update all UI text immediately without page reload when user selects a different language

6. Should translate all static UI text including:
   - Table headers (Title, Status, Priority, Actions)
   - Action buttons (Info, Edit, Delete, Create task)
   - Modal titles and footer buttons
   - Form field labels and placeholders
   - Validation error messages

7. Should translate status and priority values displayed in tags:
   - TODO → "To Do" / "Por hacer"
   - IN_PROGRESS → "In Progress" / "En progreso"
   - DONE → "Done" / "Hecho"
   - LOW → "Low" / "Baja"
   - MEDIUM → "Medium" / "Media"
   - HIGH → "High" / "Alta"

8. Should translate status and priority options in Create and Edit modal dropdowns

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should render language switcher dropdown with `EN` and `ES` options
   - Should change current language when user selects another language option
3. **E2E**
   - Should switch all UI text and status/priority tag values to Spanish when ES is selected
4. **Accessibility**
   - Language selector should be keyboard navigable
5. **UAT** - N/A

## Technical Notes
- Language detection uses `i18next-browser-languagedetector` (reads `navigator.language`)
- Translations stored in `src/locales/{lang}/translation.json`
- i18next configured in `src/i18n.ts`, imported in `src/index.tsx` and `src/setupTests.ts`
- `LanguageSwitcher` component uses PrimeReact `Dropdown`
- `DataTable` uses `key={i18n.resolvedLanguage}` to force re-render on language change
- Status/priority translation keys use API enum values directly (e.g. `common.taskStatus.TODO`) for direct lookup without mapping
- Adding a new language requires: new `src/locales/{lang}/translation.json` + registering resources in `src/i18n.ts`
- CSS class: `language-switcher`
- Test ID: `language-switcher`
