# CONFIG-001: CORS Configuration

**Points:** 2 · **Epic:** System Configuration

As a frontend app, I want CORS enabled so I can call the API from another origin.

## Acceptance Criteria

1. Should return CORS headers on preflight from any origin
2. Should allow all methods, all origin patterns, credentials, on all paths (`/**`)

## Test Plan

1. **UT** - N/A. CORS configuration has no branching logic, and the class is excluded from coverage rather than padded with a test
2. **IT** - N/A. Same exclusion: the mapping is framework configuration, not a behaviour the suite re-proves
3. **Pact** - N/A
4. **E2E** - Implicitly tested (frontend can make requests)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

## Notes

- Restrict allowed origins in production
