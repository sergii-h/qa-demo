# CONFIG-002: Error Handling

**Epic:** System Configuration  
**Priority:** High  
**Story Points:** 3

## Description
As an API consumer, I want consistent error responses so that I can handle errors appropriately in my application.

## Acceptance Criteria

1. Should return HTTP 400 with map of field names to error messages when validation error occurs

2. Should return HTTP 400 with all validation errors when multiple fields are invalid

3. Should return HTTP 404 with error message including request URI when resource not found (NoSuchElementException)

4. Should return error in format `{"fieldName": "error message"}` for validation errors

5. Should handle NoSuchElementException consistently across all endpoints returning HTTP 404

6. Should handle MethodArgumentNotValidException consistently across all endpoints returning HTTP 400

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should return HTTP 400 with field errors for validation failures
   - Should return HTTP 404 with message for NoSuchElementException
   - Should handle multiple validation errors in single response
   - Should format error responses consistently
3. **Pact**
   - Consumer: Covered by consumer tests with error scenarios
   - Provider: Should verify provider contract for error responses across all endpoints
4. **E2E** - Error messages displayed in UI (create/edit modals)
5. **UAT** - N/A

## Technical Notes
- Global exception handler using `@ControllerAdvice`
- Handles `MethodArgumentNotValidException` for validation errors
- Handles `NoSuchElementException` for not found errors
- Returns structured error responses with appropriate HTTP status codes

