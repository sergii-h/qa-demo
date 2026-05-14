# ADR 002: Test Context Pattern with Object Comparison Over Field-by-Field Assertions

## Status
**Accepted** - January 3, 2026  
**Updated** - January 26, 2026 (Added mutation approach for complex DTOs)

## Context

In unit testing domain objects with multiple fields, we need to validate that business logic correctly transforms input data into expected output.

### The Problem

Field-by-field assertions are verbose, error-prone, and don't scale:

```java
Task capturedTask = taskCaptor.getValue();
assertEquals("Test Task", capturedTask.getTitle());
assertEquals("Test Description", capturedTask.getDescription());
assertEquals(TaskStatus.TODO, capturedTask.getStatus());
assertEquals(TaskPriority.HIGH, capturedTask.getPriority());
assertNotNull(capturedTask.getId());
assertNotNull(capturedTask.getCreatedDate());
assertNotNull(capturedTask.getUpdatedDate());
// ... easy to forget fields, hard to maintain
```

**Drawbacks:**
- Verbose (8+ assertions per test)
- High maintenance burden (new field = update all tests)
- Inconsistent (different tests validate different subsets)
- Error-prone (easy to forget fields)
- Doesn't scale to complex DTOs (50+ fields = 50+ lines)

## Decision

Adopt **Test Context Pattern with Object Comparison**: create builder-based test context classes that generate test data and validate complete objects.

### Two Approaches Based on DTO Complexity

#### 1. Immutable Approach (Simple DTOs < 10 fields)

```java
Task expectedTask = context.createTaskWithGeneratedFields(
    capturedTask.getId(),
    capturedTask.getCreatedDate(),
    capturedTask.getUpdatedDate()
);
assertEquals(expectedTask, capturedTask);
```

#### 2. Mutation Approach (Complex DTOs 10+ fields) - **Recommended**

```java
Task capturedTask = taskCaptor.getValue();

// Update context with dynamic fields to validate complete object
context.setId(capturedTask.getId());
context.setCreatedDate(capturedTask.getCreatedDate());
context.setUpdatedDate(capturedTask.getUpdatedDate());

assertThat(capturedTask, is(context.createTask())); // ALL fields validated
```

## Implementation

### Test Context Class

```java
/**
 * Test Context Pattern for Task entity validation.
 * 
 * For complex DTOs (10+ fields): Use mutation approach to set dynamic fields
 * (IDs, timestamps) then validate complete object. This enables 3-5 line validation
 * instead of 50+ line field-by-field assertions.
 * 
 * See ADR-002 for full details and rationale.
 */
@Builder
@Data
public class TaskTestContext {
    @Builder.Default
    private String id = String.valueOf(new ObjectId());
    @Builder.Default
    private String title = "Test Task";
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;
    // ... other fields with defaults
    
    public TaskRequest createTaskRequest() { /* ... */ }
    public Task createTask() { /* ... */ }
}
```

**See:** `demo-service/src/test/java/com/example/demo/context/TaskTestContext.java` for complete implementation.

### Test Pattern

```java
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {
    TaskTestContext context;

    @BeforeEach
    void beforeEach() {
        context = TaskTestContext.builder()
            .title("Test Task")
            .status(TaskStatus.TODO)
            .build();
    }

    @Test
    void shouldCallInsertWithTask() {
        // given
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

        // when
        taskController.createTask(context.createTaskRequest());

        // then
        verify(repository).insert(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();
        
        // Update context with dynamic fields to validate complete object
        context.setId(capturedTask.getId());
        context.setCreatedDate(capturedTask.getCreatedDate());
        context.setUpdatedDate(capturedTask.getUpdatedDate());

        assertThat(capturedTask, is(context.createTask())); // ALL fields validated
    }
}
```

**See:** `demo-service/src/test/java/com/example/demo/TaskControllerTest.java` for complete examples.

## Consequences

### Positive ✅

1. **Scales to Any DTO Size** - Works equally well for 5 or 100+ fields (3-5 lines vs 50+)
2. **Complete Object Validation** - All fields validated automatically; new fields = tests fail until handled
3. **Improved Readability** - Test intent is clear; minimal boilerplate
4. **Enhanced Maintainability** - Single source of truth; add field once in context, not in every test
5. **Better Type Safety** - Compiler catches issues; refactoring support
6. **Consistency** - Same pattern across unit and integration tests

### Negative ⚠️

1. **Initial Setup Cost** - Requires creating context classes (one-time effort)
2. **Learning Curve** - Team needs to understand pattern
3. **Mutation Trade-offs** (complex DTOs only):
   - Violates immutability principle
   - Not thread-safe (acceptable for sequential unit tests)
   - Context reuse requires care
   - **Mitigation:** Document clearly; use context once per assertion

### Trade-off Analysis (50-field DTO)

| Metric | Mutation | Immutable | Field-by-Field |
|--------|----------|-----------|----------------|
| Lines of code | 3-5 | 50+ | 50+ |
| Complete validation | ✅ | ✅ | ⚠️ Partial |
| Readability | ⭐⭐⭐ | ⭐ | ⭐ |
| Maintainability | ⭐⭐⭐ | ⭐ | ⭐ |
| Scales to 100 fields | ✅ | ❌ | ❌ |
| Immutability | ❌ | ✅ | N/A |

**Winner:** Mutation approach (5 of 6 criteria)

## Alternatives Considered & Rejected

1. **Field-by-Field Assertions** - Verbose, doesn't scale, easy to miss fields
2. **Hamcrest `hasProperty()`** - Still verbose, string-based (not type-safe)
3. **Mockito `argThat()` Lambda** - Poor error messages, not reusable
4. **AssertJ Field Comparison** - Still verbose, reflection-based
5. **Custom Matchers** - Duplicates validation logic, high maintenance
6. **Builder with All Fields** - 50+ lines per test, unworkable for complex DTOs
7. **Field Exclusion** (`ignoringFields()`) - Loses complete validation safety net

See full analysis in ADR for detailed reasons.

## When to Use

### ✅ Use Test Context Pattern When:
- Domain object has **3+ fields** to validate
- Need **complete object validation** (not partial)
- Same test data used across multiple tests
- Complex initialization or data transformations

### Choosing Between Approaches:

**Use Mutation** (recommended):
- Complex DTOs (10+ fields)
- Few dynamic fields (2-5) vs many static (50+)
- Unit tests (sequential execution)

**Use Immutable**:
- Simple DTOs (< 10 fields)
- Few dynamic fields (2-3)
- Factory methods remain readable (< 5 parameters)

### ❌ Don't Use When:
- Simple value objects (1-2 fields)
- One-off test data (single test)
- Partial validation is intentional

## Best Practices

1. **Document Mutation** - Always comment: `// Update context with dynamic fields to validate complete object`
2. **Mutate Only Dynamic Fields** - IDs and timestamps only (system-generated), never business data
3. **Single Context Per Assertion** - Use once or create separate contexts
4. **Meaningful Names** - `existingContext`, `updateContext` (not `context1`, `context2`)
5. **Use @BeforeEach** - Define common test data once
6. **JavaDoc Context Classes** - Explain pattern and rationale

## Related Decisions

- Integration tests already use this pattern (`ItemTestContext`)
- Domain objects use Lombok `@Value`/`@Data` for proper `equals()`
- Project follows Builder Pattern for object construction

## References

- **Implementation Example:** `demo-service/src/test/java/com/example/demo/TaskControllerTest.java`
- **Context Class:** `demo-service/src/test/java/com/example/demo/context/TaskTestContext.java`
- **Testing Standards:** `doc/testing-standards.md`
- **Backend Unit Testing:** `doc/rules/backend-unit-testing.md`
- **Martin Fowler - Test Data Builder:** https://martinfowler.com/bliki/ObjectMother.html

## Revision History

- **2026-01-03:** Initial version - Adopted Test Context Pattern with Object Comparison
- **2026-01-26:** Added mutation approach for complex DTOs (10+ fields) as recommended implementation
