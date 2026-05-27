# UI-007: Language Selection

**Points:** 5 · **Epic:** Language Support

As a user, I want EN/ES with browser detection and a manual switcher so I can use my preferred language.

## Acceptance Criteria

1. Should default to Spanish on load for `es` / `es-*` browser locale; English otherwise
2. Should show `EN` / `ES` dropdown (top-right); switch language instantly without reload
3. Should translate static UI (table, buttons, modals, labels, errors) and status/priority tags and dropdown labels:
   - TODO → To Do / Por hacer; IN_PROGRESS → In Progress / En progreso; DONE → Done / Hecho
   - LOW/MEDIUM/HIGH → Low/Baja, Medium/Media, High/Alta

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should render language switcher dropdown with `EN` and `ES` options
   - Should change current language when user selects another language option
3. **Pact** - N/A
4. **E2E**
   - Should switch all UI text and status/priority tag values to Spanish when ES is selected
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual**
   - Visual check — switcher placement, translated UI and tag labels (EN/ES)
   - Keyboard navigation for language selector

## Notes

- `i18next` + `src/locales/{en,es}/translation.json`; test ID `language-switcher`
