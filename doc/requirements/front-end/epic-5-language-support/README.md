# Epic 5: Language Support

**Goal:** Allow users to use the application in their preferred language with automatic browser-based detection and a manual switcher.

## Overview
This epic covers internationalisation (i18n) of the frontend application. All UI text is extracted to locale files, browser language is detected automatically, and a language switcher lets users override the detected language at runtime.

## User Stories
- **[UI-007](UI-007-language-selection.md)** - Language Selection (5 pts)

**Total Story Points:** 5

## Key Features
- Automatic language detection from browser settings
- Manual language switcher dropdown (top-right of page)
- Supported languages: English (`en`), Spanish (`es`)
- English fallback for unsupported locales
- All UI text, form labels, error messages, and enum display values translated
- Instant re-render on language switch — no page reload

## UI Components
- PrimeReact Dropdown (language switcher)

## Dependencies
- `i18next` — core i18n library
- `react-i18next` — React bindings and `useTranslation` hook
- `i18next-browser-languagedetector` — automatic browser language detection

## File Structure
```
src/
├── i18n.ts                        # i18next initialisation
└── locales/
    ├── en/
    │   └── translation.json       # English strings
    └── es/
        └── translation.json       # Spanish strings
```

## Translation Key Structure
```json
{
  "common": {
    "taskStatus": { "TODO": "...", "IN_PROGRESS": "...", "DONE": "..." },
    "taskPriority": { "LOW": "...", "MEDIUM": "...", "HIGH": "..." }
  },
  "tasksTable": { ... },
  "createTaskModal": { ... },
  "editTaskModal": { ... },
  "infoTaskModal": { ... }
}
```
