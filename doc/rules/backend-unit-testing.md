---
type: testing-standards
technology: spring-boot, java
test-type: unit-testing
frameworks: junit5, mockito, hamcrest, jacoco
audience: developers, ai-assistants
applies-to: backend, spring-boot-applications
---

# Spring Boot Unit Testing Standards

## How to Use This Document

### Understanding Rule Severity

**MANDATORY**: Rules are marked with three severity levels throughout this document.

**Examples**:

- **RULE**: Standard practice for consistency and quality - follow for maintainable code
- **MANDATORY**: Required practice - violations will fail code review
- **CRITICAL RULE**: Non-negotiable requirement - violations will break builds or cause system failures

### Document Navigation

**RULE**: Use this guide to navigate the document efficiently based on your current task.

**Examples**:

- **Before writing tests**: Start with "Quick Reference Patterns" → Review "Pre-Implementation Checklist"
- **While writing tests**: Reference specific sections (Service Layer, Mocking Strategy, etc.)
- **During code review**: Check "Common Pitfalls to Avoid" → Verify against "AI Assistant Instructions"
- **For troubleshooting**: Use section titles to find relevant patterns and examples

---

## Quick Reference Patterns

### Standard Unit Test Setup

**CRITICAL RULE**: All unit tests must use this exact setup pattern - never use `@SpringBootTest` for unit tests.

**Examples**:

```java
// ✅ Correct: Lightweight unit test with mocked dependencies
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;
    
    @Test
    void shouldReturnExpectedResultWhenScenarioOccurs() {
        // Given: setup test data and mocks
        // When: execute the method under test
        // Then: assert expected outcomes
    }
}
```

### Essential Do's and Don'ts

**MANDATORY**: Follow these core practices for all unit tests.

**Examples**:

✅ **DO**:
- Use `@ExtendWith(MockitoExtension.class)` for unit tests
- Mock all external dependencies (DB, REST, queues, cache)
- Follow `should<Behavior>When<Condition>` naming convention
- Use Given-When-Then structure with comments
- Cover all business logic and requirements specified in the ticket
- Test edge cases and error scenarios
- Maintain 90%+ code coverage
- Keep tests under 100ms

❌ **DON'T**:
- Use `@SpringBootTest` for unit tests (integration tests only)
- Test private methods directly
- Use real external dependencies
- Mock the class under test
- Use `Thread.sleep()` in tests
- Write tests that depend on execution order
- Test simple getters/setters without logic
- Catch exceptions with try-catch (use `assertThrows()` instead)

---

## Key Principles

### Focus on Isolation

**RULE**: Unit tests must focus on testing business logic and behavior in isolation from external dependencies.

**Examples**:
- Use mocks to isolate the unit under test from databases, REST clients, message queues, and other external systems
- Test one class or method at a time
- Ensure tests are independent and don't rely on shared state

### Comprehensive Coverage

**MANDATORY**: Cover all business logic and functional requirements from the ticket description that are testable at unit level (focusing on logic that can be tested in isolation).

**Examples**:
- Map each requirement to specific test cases
- Cover all code paths and edge cases
- Test boundary conditions (null values, empty collections, min/max values)
- Test error handling and exception scenarios

### Test Independence

**CRITICAL RULE**: Tests must be independent, deterministic, and fast (< 100ms per test).

**Examples**:
- No shared state between tests
- No dependencies on test execution order
- No time-based waits or `Thread.sleep()`
- Tests should be parallelizable by default

---

## Framework and Tools

### Testing Framework Stack

**MANDATORY**: Use the following frameworks for unit testing.

**Examples**:

```java
// JUnit 5 (Jupiter) for testing framework
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    // Mockito for mocking dependencies
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldReturnUserWhenDataIsValid() {
        // Hamcrest for readable assertions
        assertThat(user.getEmail(), equalTo("john@example.com"));
    }
}
```

Required frameworks:
- **JUnit 5 (Jupiter)** - Testing framework
- **Mockito** - Mocking dependencies
- **Hamcrest** - Readable assertions
- **JaCoCo** - Code coverage analysis

### Code Coverage Requirements

**MANDATORY**: Maintain minimum 90% code coverage across all metrics.

**Examples**:

Coverage thresholds:
- Statements: 90%
- Branches: 90%
- Functions: 90%
- Lines: 90%

Build configuration:
- Fail builds if coverage drops below threshold
- Generate coverage reports on every build
- Exclude generated code and configuration classes from coverage

What NOT to test for coverage:
- Generated code (Lombok, MapStruct)
- Configuration classes (`@Configuration`)
- DTOs without logic
- Simple getters/setters
- Main application class

Focus coverage on:
- Business logic in services
- Validation logic
- Error handling
- Conditional statements and branches
- Edge cases

---

## What to Test

### Understanding Test Scope by Layer

**RULE**: Different application layers require different testing approaches and coverage - understand what to test at each layer.

**Examples**:

This section covers:
- **Service Layer**: Business logic, validation, error handling, edge cases
- **Controller Layer**: Request/response mapping, HTTP status codes, exception handling
- **Repository Layer**: Custom query logic only (standard CRUD in integration tests)
- **Utility Classes**: Pure functions, data transformations, calculations

### Service Layer Testing

**RULE**: Test all business logic, validation, error handling, edge cases, and different code paths in services.

**Examples**:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldSaveAndReturnUserWhenDataIsValid() {
        // Given
        UserDto userDto = new UserDto("john@example.com", "password123");
        User savedUser = new User(1L, "john@example.com", "encoded_password");
        
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        User result = userService.createUser(userDto);
        
        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.getEmail(), equalTo("john@example.com"));
        assertThat(result.getPassword(), equalTo("encoded_password"));
        
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFoundById() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> userService.findUserById(userId));
        
        // Then
        assertThat(exception.getMessage(), equalTo("User not found with id: 1"));
        verify(userRepository).findById(userId);
    }
}
```

### Controller Layer Testing

**RULE**: Test request/response mapping, validation, HTTP status codes, and exception handling in controllers.

**Examples**:

```java
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;
    
    @InjectMocks
    private UserController userController;
    
    @Test
    void shouldReturnCreatedStatusWhenRequestIsValid() {
        // Given
        UserDto userDto = new UserDto("john@example.com", "password123");
        User createdUser = new User(1L, "john@example.com", "encoded_password");
        
        when(userService.createUser(userDto)).thenReturn(createdUser);
        
        // When
        ResponseEntity<User> response = userController.createUser(userDto);
        
        // Then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().getId(), equalTo(1L));
        assertThat(response.getBody().getEmail(), equalTo("john@example.com"));
        
        verify(userService).createUser(userDto);
    }
    
    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFoundById() {
        // Given
        Long userId = 1L;
        when(userService.findUserById(userId))
            .thenThrow(new UserNotFoundException("User not found with id: " + userId));
        
        // When & Then
        assertThrows(UserNotFoundException.class, 
            () -> userController.getUserById(userId));
        
        verify(userService).findUserById(userId);
    }
}
```

### Repository Testing

**RULE**: Test only custom query logic at unit level; test standard CRUD operations at integration level.

**Examples**:
- Custom JPA queries with `@Query` annotation
- Specification-based queries
- Query methods with complex logic
- Native SQL queries

### Utility Class Testing

**RULE**: Test pure functions, helper methods, data transformations, and calculations in utility classes.

**Examples**:

```java
class DateUtilsTest {
    
    @Test
    void shouldReturnCorrectAgeWhenBirthDateIsInPast() {
        // Given
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        LocalDate currentDate = LocalDate.of(2025, 5, 15);
        
        // When
        int age = DateUtils.calculateAge(birthDate, currentDate);
        
        // Then
        assertThat(age, equalTo(35));
    }
    
    @Test
    void shouldReturnZeroWhenBirthDateIsToday() {
        // Given
        LocalDate birthDate = LocalDate.now();
        LocalDate currentDate = LocalDate.now();
        
        // When
        int age = DateUtils.calculateAge(birthDate, currentDate);
        
        // Then
        assertThat(age, equalTo(0));
    }
    
    @Test
    void shouldThrowExceptionWhenBirthDateIsInFuture() {
        // Given
        LocalDate birthDate = LocalDate.now().plusYears(1);
        LocalDate currentDate = LocalDate.now();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> DateUtils.calculateAge(birthDate, currentDate));
        assertThat(exception.getMessage(), equalTo("Birth date cannot be in the future"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "@example.com", "user@"})
    void shouldThrowExceptionWhenEmailFormatIsInvalid(String invalidEmail) {
        // When & Then
        assertThrows(InvalidEmailException.class,
            () -> EmailValidator.validate(invalidEmail));
    }
}
```

---

## Mocking Strategy

### Decision Guide for Mocking

**CRITICAL RULE**: Proper mocking is essential for unit test isolation - mock external dependencies, never mock the class under test or simple value objects.

**Examples**:

Use this decision guide:
- **External system** (database, REST API, message queue, cache) → **Mock it**
- **Service or repository dependency** → **Mock it**
- **The class being tested** → **Never mock it**
- **POJO/DTO/Value object** → **Never mock it** (use real instances)
- **Java standard library** (String, List, Map) → **Never mock it**
- **Simple utility without dependencies** → **Never mock it**

### What to Mock

**MANDATORY**: Mock external dependencies and collaborators to isolate the unit under test.

**Examples**:

Mock these dependencies:
- **External dependencies:**
  - Databases and repositories
  - REST clients and HTTP services
  - Message queues (Kafka, RabbitMQ)
  - Cache systems (Redis, Memcached)
- **Services** (when testing controllers or other services)
- **Repositories** (when testing services)
- **Third-party libraries** with external side effects
- **Time-dependent operations** (`Clock`, date/time utilities)

### What NOT to Mock

**CRITICAL RULE**: Do not mock the class under test, POJOs, value objects, or Java standard library.

**Examples**:

Do NOT mock:
- The class under test itself
- POJOs/DTOs (data transfer objects)
- Value objects (immutable domain objects)
- Java standard library (String, List, Map, etc.)
- Configuration values (use actual values or test properties)
- Simple utility classes with no external dependencies

---

## Test Structure and Organization

### Importance of Consistent Structure

**MANDATORY**: Consistent structure makes tests readable, maintainable, and navigable - follow these conventions for all tests.

**Examples**:

Key aspects covered:
- **File naming**: `<ClassUnderTest>Test.java` pattern
- **Method naming**: `should<Behavior>When<Condition>` pattern
- **Test structure**: Given-When-Then organization
- **Grouping**: `@Nested` classes for related tests
- **Parameterization**: `@ParameterizedTest` for multiple scenarios

### File Naming and Location

**MANDATORY**: Follow consistent naming conventions and package structure for test files.

**Examples**:

Test file naming:
- Unit test files must end with `Test.java` (NOT `UnitTest.java`)
- Pattern: `<ClassUnderTest>Test.java`
- Examples: `UserServiceTest.java`, `OrderControllerTest.java`, `DateUtilsTest.java`

Location:
- Place unit tests in `src/test/java`
- Mirror the package structure of the code under test
- One test class per production class

### Test Method Naming

**MANDATORY**: Use descriptive test method names following the pattern: `should<Behavior>When<Condition>`.

**Examples**:

```java
@Test
void shouldReturnUserWhenDataIsValid() { }

@Test
void shouldThrowValidationExceptionWhenEmailIsInvalid() { }

@Test
void shouldReturnZeroWhenCartIsEmpty() { }

@Test
void shouldThrowPaymentExceptionWhenFundsAreInsufficient() { }
```

### Given-When-Then Structure

**RULE**: Structure test methods using Given-When-Then pattern for clarity and readability.

**Examples**:

```java
@Test
void shouldReturnUserWhenDataIsValid() {
    // Given: Set up test data and mocks
    UserDto userDto = new UserDto("john@example.com", "password");
    User expectedUser = new User("john@example.com");
    when(userRepository.save(any(User.class))).thenReturn(expectedUser);
    
    // When: Execute the method under test
    User result = userService.createUser(userDto);
    
    // Then: Assert expected outcomes
    assertThat(result.getEmail(), equalTo("john@example.com"));
    verify(userRepository).save(any(User.class));
}
```

### Nested Tests

**RULE**: Use `@Nested` classes with `@DisplayName` to group related tests by feature or method.

**Examples**:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {
        
        @Test
        @DisplayName("should create user with valid data")
        void shouldCreateUserWhenDataIsValid() {
            // Given
            UserDto userDto = new UserDto("john@example.com", "password");
            User savedUser = new User(1L, "john@example.com", "encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            
            // When
            User user = userService.createUser(userDto);
            
            // Then
            assertThat(user.getEmail(), equalTo("john@example.com"));
            verify(userRepository).save(any(User.class));
        }
        
        @Test
        @DisplayName("should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // Given
            UserDto userDto = new UserDto("invalid-email", "password");
            
            // When & Then
            assertThrows(ValidationException.class,
                () -> userService.createUser(userDto));
        }
        
        @Test
        @DisplayName("should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            UserDto userDto = new UserDto("john@example.com", "password");
            when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);
            
            // When & Then
            assertThrows(DuplicateEmailException.class,
                () -> userService.createUser(userDto));
        }
    }
    
    @Nested
    @DisplayName("Find User Tests")
    class FindUserTests {
        
        @Test
        @DisplayName("should return user when found by id")
        void shouldReturnUserWhenFoundById() {
            // Given
            Long userId = 1L;
            User user = new User(userId, "john@example.com", "password");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            
            // When
            User result = userService.findUserById(userId);
            
            // Then
            assertThat(result, is(notNullValue()));
            assertThat(result.getId(), equalTo(userId));
        }
        
        @Test
        @DisplayName("should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            
            // When & Then
            assertThrows(UserNotFoundException.class,
                () -> userService.findUserById(userId));
        }
    }
}
```

### Parameterised Tests

**RULE**: Use `@ParameterizedTest` to test multiple scenarios with different inputs efficiently.

**Examples**:

```java
// Using @ValueSource for simple values
@ParameterizedTest
@ValueSource(strings = {"", " ", "invalid-email", "@example.com", "user@"})
void shouldThrowExceptionWhenEmailFormatIsInvalid(String invalidEmail) {
    // When & Then
    assertThrows(InvalidEmailException.class,
        () -> EmailValidator.validate(invalidEmail));
}

// Using @CsvSource for multiple parameters
@ParameterizedTest
@CsvSource({
    "1990-01-01, 2025-01-01, 35",
    "2000-06-15, 2025-06-15, 25",
    "2024-11-18, 2025-11-18, 1"
})
void shouldReturnCorrectAgeWhenCalculatingWithVariousDates(
    LocalDate birthDate, LocalDate currentDate, int expectedAge) {
    // When
    int age = DateUtils.calculateAge(birthDate, currentDate);
    
    // Then
    assertThat(age, equalTo(expectedAge));
}

// Using @CsvSource for calculations
@ParameterizedTest
@CsvSource({
    "0, 0.0",
    "1, 10.0",
    "5, 50.0",
    "10, 100.0"
})
void shouldReturnCorrectTotalWhenCalculatingWithQuantity(int quantity, double expected) {
    // When
    double total = calculator.calculateTotal(quantity, 10.0);
    
    // Then
    assertThat(total, equalTo(expected));
}

// Using @MethodSource for complex objects
@ParameterizedTest
@MethodSource("provideInvalidUserData")
void shouldThrowValidationExceptionWhenUserDataIsInvalid(UserDto userDto) {
    // When & Then
    assertThrows(ValidationException.class, 
        () -> userService.createUser(userDto));
}

private static Stream<UserDto> provideInvalidUserData() {
    return Stream.of(
        new UserDto("", "password"),           // Empty email
        new UserDto("invalid-email", "password"),  // Invalid format
        new UserDto("john@example.com", "")    // Empty password
    );
}
```

---

## Verification and Assertions

### Effective Verification Techniques

**RULE**: Use proper verification and assertions to validate behavior, not implementation details.

**Examples**:

This section covers:
- **Mockito verification**: Using `verify()` to assert interactions with mocks
- **Hamcrest matchers**: Readable assertions with `assertThat()`, `equalTo()`, `is()`
- **What to verify**: Critical behaviors vs. implementation details
- **When to verify**: Interaction-based testing vs. state-based testing

### Mockito Verification

**RULE**: Use Mockito's `verify()` to assert that interactions with mocked dependencies occurred as expected.

**Examples**:

```java
// ✅ Verify method was called with specific arguments
verify(userRepository).save(user);

// ✅ Verify method was called exactly once
verify(userService, times(1)).createUser(userDto);

// ✅ Verify method was never called
verify(emailService, never()).sendEmail(anyString());

// ✅ Verify method was called with any argument of specific type
verify(userRepository).save(any(User.class));

// ✅ Verify method was called with argument matching condition
verify(userRepository).save(argThat(user -> 
    user.getEmail().equals("john@example.com")
));

// ✅ Verify order of invocations
InOrder inOrder = inOrder(userRepository, emailService);
inOrder.verify(userRepository).save(user);
inOrder.verify(emailService).sendWelcomeEmail(user.getEmail());
```

### Hamcrest Matchers

**RULE**: Use Hamcrest matchers for readable and expressive assertions.

**Examples**:

```java
// Basic assertions - null checks and equality
assertThat(user, is(notNullValue()));
assertThat(user.getEmail(), equalTo("john@example.com"));
assertThat(user.getActive(), is(true));

// Numeric comparisons
assertThat(user.getAge(), greaterThan(18));
assertThat(user.getAge(), lessThan(100));
assertThat(balance, greaterThanOrEqualTo(BigDecimal.ZERO));

// Collection assertions
assertThat(users, hasSize(3));
assertThat(users, is(empty()));
assertThat(userEmails, containsInAnyOrder("john@example.com", "jane@example.com", "admin@example.com"));
assertThat(users, everyItem(hasProperty("status", equalTo("ACTIVE"))));

// String assertions
assertThat(message, containsString("success"));
assertThat(email, startsWith("admin@"));
assertThat(email, endsWith("@example.com"));
assertThat(code, matchesRegex("[A-Z]{3}\\d{3}"));

// Combining matchers with allOf (AND)
assertThat(user, allOf(
    hasProperty("email", equalTo("john@example.com")),
    hasProperty("status", equalTo("ACTIVE")),
    hasProperty("age", greaterThan(18))
));

// Combining matchers with anyOf (OR)
assertThat(user.getEmail(), anyOf(
    containsString("@gmail.com"),
    containsString("@yahoo.com")
));

// Exception assertions with JUnit 5
UserNotFoundException exception = assertThrows(UserNotFoundException.class,
    () -> userService.deleteUser(userId));
assertThat(exception.getMessage(), containsString("User not found with id:"));
```

---

## Testing Special Scenarios

### Handling Complex Test Cases

**RULE**: Special scenarios like async code and exceptions require specific testing patterns.

**Examples**:

This section covers:
- **Asynchronous code**: Testing with CompletableFuture and async operations
- **Exception handling**: Using `assertThrows()` properly
- **Edge cases**: Null values, empty collections, boundary conditions
- **Time-dependent code**: Mocking Clock and date/time utilities

### Testing Asynchronous Code

**RULE**: Mock async dependencies to return CompletableFuture. Verify completion status and test both success/failure scenarios.

**Examples**:

```java
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    
    @Mock
    private EmailClient emailClient;
    
    @InjectMocks
    private EmailService emailService;
    
    @Test
    void sendEmail_Async_CompletesSuccessfully() {
        // Given
        String email = "user@example.com";
        when(emailClient.send(anyString()))
            .thenReturn(CompletableFuture.completedFuture(true));
        
        // When
        CompletableFuture<Boolean> result = emailService.sendEmailAsync(email);
        
        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.isDone(), is(true));
        assertThat(result.join(), is(true));
        verify(emailClient).send(email);
    }
    
    @Test
    void sendEmail_Async_FailsWithException() {
        // Given
        String email = "user@example.com";
        when(emailClient.send(anyString()))
            .thenReturn(CompletableFuture.failedFuture(
                new EmailException("Failed to send")
            ));
        
        // When
        CompletableFuture<Boolean> result = emailService.sendEmailAsync(email);
        
        // Then
        assertThat(result.isCompletedExceptionally(), is(true));
        verify(emailClient).send(email);
    }
    
    @Test
    void shouldCompleteSuccessfullyWhenProcessingAsyncWithValidData() {
        // Given
        UserDto userDto = new UserDto("john@example.com", "password");
        User user = new User(1L, "john@example.com", "encoded_password");
        CompletableFuture<User> future = CompletableFuture.completedFuture(user);
        
        when(asyncService.createUserAsync(userDto)).thenReturn(future);
        
        // When
        CompletableFuture<User> result = userService.processAsync(userDto);
        
        // Then
        assertThat(result.isDone(), is(true));
        assertThat(result.join().getEmail(), equalTo("john@example.com"));
    }
}
```

### Testing Exception Handling

**MANDATORY**: Use JUnit 5's `assertThrows()` to test exception scenarios properly.

**Examples**:

```java
@Test
void shouldThrowValidationExceptionWhenEmailIsInvalid() {
    // Given
    UserDto userDto = new UserDto("invalid-email", "password");
    
    // When & Then
    ValidationException exception = assertThrows(
        ValidationException.class,
        () -> userService.createUser(userDto)
    );
    
    // Verify exception message
    assertThat(exception.getMessage(), containsString("invalid email"));
}

@Test
void shouldThrowNotFoundExceptionWhenDeletingNonExistentUser() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(NotFoundException.class, 
        () -> userService.deleteUser(1L));
}
```

---

## Performance Guidelines

### Why Test Speed Matters

**CRITICAL RULE**: Fast tests enable rapid feedback, TDD workflows, and efficient CI/CD pipelines - unit tests must complete in under 100ms.

**Examples**:

Performance impacts:
- **< 100ms per test**: Enables TDD and instant feedback
- **Thousands of tests in seconds**: Efficient development workflow
- **Fast CI/CD**: Quick build and deployment cycles
- **Parallel execution**: Independent tests run simultaneously

### Test Execution Speed

**CRITICAL RULE**: Unit tests must be fast to enable rapid feedback and efficient CI/CD pipelines.

**Examples**:

Speed targets:
- **Target**: < 100ms per unit test
- **Maximum**: 500ms per unit test
- If tests are slower, you're likely doing integration testing

Why speed matters:
- Fast feedback loop for developers
- Enable TDD workflow
- Thousands of tests should run in seconds
- CI/CD pipeline efficiency

Keep tests fast by:
- Mocking all external dependencies
- Avoiding `Thread.sleep()` or time-based waits
- Not loading Spring context (`@SpringBootTest`)
- Not using real databases or external services
- Using simple test data
- Avoiding complex object graph creation

### Parallel Execution

**RULE**: Design unit tests to be parallelizable for faster test suite execution.

**Examples**:

Requirements for parallel execution:
- No shared static state between tests
- No dependencies on test execution order
- Each test is completely independent
- Use Maven Surefire or Gradle parallel execution configuration

---

## Unit Tests vs Integration Tests

### Choosing the Right Test Type

**MANDATORY**: Use unit tests for isolated business logic, integration tests for multi-layer interactions.

**Examples**:

Decision criteria:
- **Fast (< 100ms), mocked dependencies, single class** → Unit test
- **Slower (> 100ms), real dependencies, multiple layers** → Integration test
- **Testing business logic in isolation** → Unit test
- **Testing actual database queries** → Integration test
- **Testing REST API endpoints with real HTTP** → Integration test

### When to Use Unit Tests

**RULE**: Use unit tests for testing business logic in isolation with mocked dependencies.

**Examples**:

Use unit tests when:
- Testing business logic in isolation
- Testing input validation and edge cases
- Testing error handling and exception scenarios
- Testing calculations and data transformations
- Testing conditional logic and branching
- Fast feedback is critical (thousands of tests)
- External dependencies can be easily mocked
- Testing a single class or method in isolation

### When to Use Integration Tests

**RULE**: Use integration tests for testing interactions between multiple layers with real dependencies.

**Examples**:

Use integration tests when:
- Testing interaction between multiple layers
- Testing actual database queries and persistence
- Testing REST API endpoints with real HTTP
- Testing security and authentication flows
- Testing transactions and rollback behavior
- Testing with real Spring context and dependency injection

---

## Common Pitfalls to Avoid

### Learning from Common Mistakes

**CRITICAL RULE**: Avoid these anti-patterns that commonly appear in unit tests - each mistake breaks unit testing principles.

**Examples**:

Review these pitfalls regularly:
1. Using `@SpringBootTest` for unit tests
2. Testing private methods directly
3. Over-verification of implementation details
4. Testing getters/setters without logic
5. Catching exceptions manually
6. Writing meaningless assertions
7. Tests depending on execution order
8. Mocking the class under test
9. Using real external dependencies
10. Using `Thread.sleep()` in tests

---

### 1. Using @SpringBootTest for Unit Tests

❌ **DON'T**: Use `@SpringBootTest` for unit tests - it's too heavy and defeats the purpose of unit testing.

```java
// ❌ Bad: Using @SpringBootTest for unit test
@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;
}
```

✅ **DO**: Use `@ExtendWith(MockitoExtension.class)` for lightweight unit tests with mocked dependencies.

```java
// ✅ Good: Using Mockito for unit test
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
}
```

### 2. Testing Private Methods Directly

❌ **DON'T**: Test private methods directly using reflection - it makes tests brittle and couples them to implementation.

```java
// ❌ Bad: Testing private method with reflection
@Test
void testPrivateMethod() throws Exception {
    Method method = UserService.class.getDeclaredMethod("validateUser", User.class);
    method.setAccessible(true);
    method.invoke(userService, user);
}
```

✅ **DO**: Test private methods indirectly through the public API.

```java
// ✅ Good: Test through public API
@Test
void shouldThrowValidationExceptionWhenUserDataIsInvalid() {
    User invalidUser = new User();
    assertThrows(ValidationException.class,
        () -> userService.createUser(invalidUser));
}
```

### 3. Over-Verification of Implementation Details

❌ **DON'T**: Verify every single interaction - it makes tests brittle and coupled to implementation.

```java
// ❌ Bad: Verifying too many implementation details
@Test
void shouldSaveUserWhenDataIsValid() {
    userService.createUser(userDto);
    
    verify(userRepository).findByEmail(userDto.getEmail());
    verify(userValidator).validate(any());
    verify(passwordEncoder).encode(userDto.getPassword());
    verify(userRepository).save(any());
    verify(eventPublisher).publish(any());
    // Too many verifications - test is fragile!
}
```

✅ **DO**: Verify only the critical behavior that matters to the API contract.

```java
// ✅ Good: Verify only the critical behavior
@Test
void shouldSaveUserToRepositoryWhenDataIsValid() {
    userService.createUser(userDto);
    
    verify(userRepository).save(argThat(user ->
        user.getEmail().equals(userDto.getEmail())
    ));
}
```

### 4. Testing Getters and Setters Without Logic

❌ **DON'T**: Write tests for simple getters and setters - they provide no value.

```java
// ❌ Bad: Pointless test for simple getter/setter
@Test
void testGetName() {
    user.setName("John");
    assertThat(user.getName(), equalTo("John"));
}
```

✅ **DO**: Test only methods with actual business logic or behavior.

```java
// ✅ Good: Test actual behavior with logic
@Test
void shouldReturnCombinedNameWhenBothFirstAndLastNameProvided() {
    user.setFirstName("John");
    user.setLastName("Doe");
    assertThat(user.getDisplayName(), equalTo("John Doe"));
}
```

### 5. Catching Exceptions Manually

❌ **DON'T**: Catch exceptions manually with try-catch blocks - it's verbose and error-prone.

```java
// ❌ Bad: Catching exceptions manually
@Test
void shouldThrowExceptionWhenUserDataIsInvalid() {
    try {
        userService.createUser(invalidUser);
        fail("Expected ValidationException");
    } catch (ValidationException e) {
        assertThat(e.getMessage(), containsString("invalid"));
    }
}
```

✅ **DO**: Use JUnit 5's `assertThrows()` for clean and readable exception testing.

```java
// ✅ Good: Using assertThrows
@Test
void shouldThrowValidationExceptionWhenUserDataIsInvalid() {
    ValidationException exception = assertThrows(
        ValidationException.class,
        () -> userService.createUser(invalidUser)
    );
    assertThat(exception.getMessage(), containsString("invalid"));
}
```

### 6. Writing Meaningless Assertions

❌ **DON'T**: Write assertions that don't test actual logic or behavior.

```java
// ❌ Bad: Assertion that doesn't test actual logic
@Test
void testSomething() {
    assertThat(true, is(true));
    assertThat(new ArrayList<>(), equalTo(new ArrayList<>()));
}

// ❌ Bad: Testing obvious behavior without logic
@Test
void testUserCreation() {
    User user = new User();
    assertThat(user, is(notNullValue()));  // Always true
}
```

✅ **DO**: Test actual business logic and meaningful behavior.

```java
// ✅ Good: Test actual behavior
@Test
void shouldReturnZeroWhenCartIsEmpty() {
    BigDecimal total = calculator.calculateTotal(new ArrayList<>());
    assertThat(total, equalTo(BigDecimal.ZERO));
}

// ✅ Good: Test meaningful business logic
@Test
void shouldSetDefaultRoleWhenCreatingUser() {
    User user = userService.createUser(userDto);
    assertThat(user.getRole(), equalTo(Role.USER));
}
```

### 7. Tests Depending on Execution Order

❌ **DON'T**: Write tests that depend on execution order or shared state.

```java
// ❌ Bad: Tests depend on execution order
private static User sharedUser;

@Test
void test1_CreateUser() {
    sharedUser = userService.createUser(userDto);
}

@Test
void test2_UpdateUser() {
    sharedUser.setEmail("new@example.com");
    userService.updateUser(sharedUser);  // Depends on test1!
}
```

✅ **DO**: Make each test completely independent with its own setup.

```java
// ✅ Good: Independent tests with own setup
@BeforeEach
void setUp() {
    user = new User("john@example.com");
}

@Test
void shouldReturnUserWhenCreatingWithValidData() {
    User result = userService.createUser(userDto);
    assertThat(result.getEmail(), equalTo(userDto.getEmail()));
}

@Test
void shouldUpdateSuccessfullyWhenChangingEmail() {
    user.setEmail("new@example.com");
    User result = userService.updateUser(user);
    assertThat(result.getEmail(), equalTo("new@example.com"));
}
```

### 8. Mocking the Class Under Test

❌ **DON'T**: Mock the class you're testing - it defeats the purpose of the test.

```java
// ❌ Bad: Mocking the class under test
@Mock
private UserService userService;  // This is what we should be testing!

@Test
void testCreateUser() {
    when(userService.createUser(any())).thenReturn(user);
    User result = userService.createUser(userDto);
    // We're not actually testing anything!
}
```

✅ **DO**: Mock only the dependencies, not the class under test.

```java
// ✅ Good: Mock dependencies, test the actual class
@Mock
private UserRepository userRepository;

@Mock
private PasswordEncoder passwordEncoder;

@InjectMocks
private UserService userService;  // Real instance being tested

@Test
void shouldSaveUserWhenCreatingWithValidData() {
    when(userRepository.save(any())).thenReturn(savedUser);
    User result = userService.createUser(userDto);
    verify(userRepository).save(any(User.class));
}
```

### 9. Using Real External Dependencies

❌ **DON'T**: Use real databases, REST clients, or external services in unit tests.

```java
// ❌ Bad: Using real database
@DataJpaTest
class UserServiceTest {
    @Autowired
    private UserRepository userRepository;  // Real database!
}
```

✅ **DO**: Mock all external dependencies for fast, isolated unit tests.

```java
// ✅ Good: Mock external dependencies
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;  // Mocked!
    
    @InjectMocks
    private UserService userService;
}
```

### 10. Using Thread.sleep() in Tests

❌ **DON'T**: Use `Thread.sleep()` or time-based waits in tests - it makes them slow and flaky.

```java
// ❌ Bad: Using Thread.sleep()
@Test
void testAsyncOperation() throws InterruptedException {
    asyncService.processData();
    Thread.sleep(5000);  // Slow and unreliable!
    verify(repository).save(any());
}
```

✅ **DO**: Mock async operations to return CompletableFuture or use proper async testing.

```java
// ✅ Good: Mock async operation
@Test
void testAsyncOperation() {
    CompletableFuture<Data> future = CompletableFuture.completedFuture(data);
    when(asyncService.processData()).thenReturn(future);
    
    CompletableFuture<Data> result = asyncService.processData();
    assertThat(result.isDone(), is(true));
}
```

---

## AI Assistant Instructions

### Before Generating Any Test Code

**CRITICAL RULE**: AI assistants must complete this preparation before generating any unit test code.

**Examples**:

1. **Read the Pre-Implementation Checklist** at the end of this document
2. **Review "Common Pitfalls to Avoid"** section to avoid anti-patterns
3. **Map ticket ACs to test cases** - this is mandatory
4. **Use "Quick Reference Patterns"** section for standard templates
5. **Verify test type decision** - ensure unit test is the right choice (not integration test)

### Mandatory Practices for AI-Generated Tests

**CRITICAL RULE**: Every test generated by AI assistants must follow these practices without exception.

**Examples**:

1. **Always use @ExtendWith(MockitoExtension.class)**: Never use `@SpringBootTest` for unit tests. Use `@ExtendWith(MockitoExtension.class)` with `@Mock` and `@InjectMocks` annotations.

2. **Map requirements to test cases**: Ensure all business logic and functional requirements from the ticket that are testable in isolation are covered by specific test cases.

3. **Follow naming conventions**: Use `<ClassUnderTest>Test.java` for class names and `should<Behavior>When<Condition>` for test method names.

4. **Use Given-When-Then structure**: Structure all test methods with clear Given (setup), When (execution), and Then (assertion) sections with comments.

5. **Mock external dependencies**: Mock databases, REST clients, message queues, caches, and all external systems. Never use real external dependencies in unit tests.

6. **Use Hamcrest matchers**: Prefer Hamcrest matchers (`assertThat`, `equalTo`, `is`) over JUnit assertions for better readability.

7. **Verify critical interactions**: Use `verify()` to assert important interactions with mocks, but avoid over-verification of implementation details.

8. **Test edge cases**: Always include tests for null values, empty collections, boundary conditions, and invalid inputs.

9. **Test exception scenarios**: Use `assertThrows()` to test exception handling properly, and verify exception messages.

10. **Use parameterised tests**: When testing multiple similar scenarios, use `@ParameterizedTest` with `@ValueSource`, `@CsvSource`, or `@MethodSource`.

11. **Group related tests**: Use `@Nested` classes with `@DisplayName` to group tests by method or feature for better organization.

12. **Keep tests fast**: Each unit test should complete in < 100ms. If slower, you're probably doing integration testing.

13. **Make tests independent**: Each test must be completely independent with no shared state or execution order dependencies.

14. **Don't test framework code**: Trust that Spring, JPA, and other frameworks work correctly. Focus on your business logic.

15. **Don't test private methods**: Test only through the public API. Private method logic should be covered by public method tests.

16. **Don't test getters/setters**: Skip simple getters/setters without logic. Only test methods with actual business behavior.

17. **Use @BeforeEach for setup**: Use `@BeforeEach` for common test setup. Avoid `@BeforeAll` unless absolutely necessary.

18. **Maintain 90% coverage**: Ensure code coverage meets or exceeds 90% threshold for statements, branches, functions, and lines.

19. **Write meaningful assertions**: Avoid trivial assertions like `assertThat(true, is(true))`. Test actual business logic and behavior.

20. **Test one thing at a time**: Keep tests focused and atomic. Each test should verify one specific behavior or scenario.

### Quick Decision Trees for AI Assistants

**RULE**: Use these decision trees for quick answers to common questions when generating tests.

**Examples**:

**Q: What annotation should I use?**
- Unit test → `@ExtendWith(MockitoExtension.class)`
- Integration test → `@SpringBootTest`

**Q: Should I mock this dependency?**
- External system (DB, REST, cache, queue) → YES, mock it
- Service or repository dependency → YES, mock it
- The class under test → NO, never mock it
- POJO/DTO/Value object → NO, use real instances
- Java standard library (String, List, Map) → NO, use real instances

**Q: What should I test?**
- Business logic → YES, test thoroughly
- All ticket ACs → YES, mandatory
- Edge cases and errors → YES, always
- Private methods → NO, test through public API
- Simple getters/setters → NO, unless they contain logic
- Framework code → NO, trust the framework

**Q: How do I know if it's a unit test or integration test?**
- Fast (< 100ms), mocked dependencies, one class → Unit test
- Slower (> 100ms), real dependencies, multiple layers → Integration test

### Final Verification Before Generating Tests

**CRITICAL RULE**: Verify these items immediately before generating any test code.

**Examples**:

✅ Checklist:
- I have reviewed the Pre-Implementation Checklist
- I am using `@ExtendWith(MockitoExtension.class)` (NOT `@SpringBootTest`)
- I have mapped all business requirements from the ticket to specific unit test cases (focusing on logic testable in isolation)
- I am following `should<Behavior>When<Condition>` naming convention
- I am using Given-When-Then structure with comments
- I am mocking all external dependencies (never mocking class under test)
- I am testing edge cases and error scenarios
- My tests are independent and will complete in < 100ms each

---

## Pre-Implementation Checklist

### Using This Checklist Effectively

**CRITICAL RULE**: This checklist consolidates ALL requirements from this document - review before writing any unit test.

**Examples**:

Checklist workflow:
1. **Before starting**: Review entire checklist to understand full scope
2. **During implementation**: Check off items as you complete them
3. **Before code review**: Verify all items are checked
4. **During review**: Confirm all items are actually implemented
5. **If item cannot be checked**: Review the relevant section above for guidance

### Required Verifications

### Test Framework Setup
- [ ] Using `@ExtendWith(MockitoExtension.class)` for unit tests
- [ ] NOT using `@SpringBootTest` for unit tests
- [ ] Dependencies are mocked with `@Mock`
- [ ] Class under test is annotated with `@InjectMocks`
- [ ] Test file follows naming convention `<ClassUnderTest>Test.java`
- [ ] Test file is in `src/test/java` with matching package structure

### Test Coverage
- [ ] All business logic and functional requirements from ticket are mapped to test cases
- [ ] All public methods have corresponding tests
- [ ] All code branches and paths are covered
- [ ] Edge cases are tested (null, empty, boundary values)
- [ ] Exception scenarios are tested
- [ ] Error handling is tested
- [ ] Code coverage meets 90% threshold

### Test Structure
- [ ] Test methods follow `should<Behavior>When<Condition>` naming convention
- [ ] Each test uses Given-When-Then structure with comments
- [ ] Related tests are grouped with `@Nested` and `@DisplayName`
- [ ] Parameterised tests are used for multiple similar scenarios
- [ ] Each test is focused on one specific behavior
- [ ] Tests are independent with no shared state

### Mocking Strategy
- [ ] All external dependencies are mocked (database, REST, queues, cache)
- [ ] Class under test is NOT mocked
- [ ] POJOs and DTOs are NOT mocked
- [ ] Java standard library is NOT mocked
- [ ] Mock setup uses `when()...thenReturn()` pattern
- [ ] Critical interactions are verified with `verify()`
- [ ] Over-verification is avoided

### Assertions and Verification
- [ ] Using Hamcrest matchers for assertions
- [ ] Using `assertThrows()` for exception testing
- [ ] Assertions test actual business logic (not trivial)
- [ ] Exception messages are verified when relevant
- [ ] Only critical behaviors are verified (not implementation details)

### Performance
- [ ] Each test completes in < 100ms
- [ ] No `Thread.sleep()` or time-based waits
- [ ] No real external dependencies (database, HTTP, etc.)
- [ ] Tests are parallelizable (no shared static state)
- [ ] Simple test data is used (no complex object graphs)

### Best Practices
- [ ] Private methods are NOT tested directly
- [ ] Simple getters/setters are NOT tested
- [ ] Framework code is NOT tested
- [ ] Tests don't depend on execution order
- [ ] `@BeforeEach` is used for common setup
- [ ] Each test is fully independent
- [ ] Test names are descriptive and clear

---

## References

- **JUnit 5 User Guide**: https://junit.org/junit5/docs/current/user-guide/
- **Mockito Documentation**: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Hamcrest Documentation**: http://hamcrest.org/JavaHamcrest/
- **JaCoCo Documentation**: https://www.jacoco.org/jacoco/trunk/doc/
- **Spring Boot Testing**: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- **ADR-002: Test Context Pattern**: ../adr/002-test-context-pattern-with-object-comparison.md

---

## Test Context Pattern for Object Validation

### Why Use Test Context Pattern

**MANDATORY**: For domain objects with 3+ fields, use the **Test Context Pattern with Object Comparison** instead of field-by-field assertions.

**Examples**:

Benefits:
- **Improved Readability**: Single object comparison instead of 8+ field assertions
- **Enhanced Maintainability**: Adding new fields only requires updating the context class
- **Prevents Incomplete Validation**: Object comparison validates ALL fields automatically
- **Better Type Safety**: Compiler catches missing fields in object construction
- **Consistency**: Same pattern used in integration tests (`ItemTestContext`)

See: [ADR-002: Test Context Pattern with Object Comparison](../adr/002-test-context-pattern-with-object-comparison.md)

### Test Context Class Structure

**RULE**: Create builder-based test context classes with sensible defaults and helper methods.

**Examples**:

```java
// ✅ Good: Test Context with builder and helper methods
@Builder
@Data
public class TaskTestContext {
    private String id;
    
    @Builder.Default
    private String title = "Test Task";
    
    @Builder.Default
    private String description = "Test Description";
    
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;
    
    @Builder.Default
    private TaskPriority priority = TaskPriority.HIGH;
    
    private Instant createdDate;
    private Instant updatedDate;

    // Helper: Create request object
    public TaskRequest createTaskRequest() {
        return TaskRequest.builder()
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .build();
    }

    // Helper: Create expected domain object
    public Task createExpectedTask() {
        return Task.builder()
                .id(id)
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .createdDate(createdDate)
                .updatedDate(updatedDate)
                .build();
    }

    // Helper: Create object with generated fields
    public Task createTaskWithGeneratedFields(String generatedId, 
                                               Instant generatedCreatedDate, 
                                               Instant generatedUpdatedDate) {
        return Task.builder()
                .id(generatedId)
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .createdDate(generatedCreatedDate)
                .updatedDate(generatedUpdatedDate)
                .build();
    }
}
```

### Using Test Context in Tests

**RULE**: Combine Test Context Pattern with ArgumentCaptor for clean, maintainable test code.

**Examples**:

```java
// ✅ Good: Using Test Context with object comparison
@Test
void shouldCreateTaskWhenValidRequestProvided() {
    // Given
    TaskTestContext context = TaskTestContext.builder()
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.TODO)
            .priority(TaskPriority.HIGH)
            .build();

    TaskRequest request = context.createTaskRequest();

    Task savedTask = Task.builder()
            .id("507f1f77bcf86cd799439011")
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.TODO)
            .priority(TaskPriority.HIGH)
            .createdDate(Instant.now())
            .updatedDate(Instant.now())
            .build();

    when(repository.findByTitle("Test Task")).thenReturn(null);
    when(repository.insert(any(Task.class))).thenReturn(savedTask);

    ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

    // When
    Task result = taskController.createTask(request);

    // Then
    verify(repository).findByTitle("Test Task");
    verify(repository).insert(taskCaptor.capture());
    verify(taskEventProducer).produceTaskCreated(savedTask);

    Task capturedTask = taskCaptor.getValue();
    Task expectedTask = context.createTaskWithGeneratedFields(
            capturedTask.getId(),
            capturedTask.getCreatedDate(),
            capturedTask.getUpdatedDate()
    );

    // Single assertion instead of 8+ field checks
    assertEquals(expectedTask, capturedTask);
    assertEquals(capturedTask.getCreatedDate(), capturedTask.getUpdatedDate());
    assertEquals(savedTask, result);
}

// ❌ Bad: Field-by-field assertions (old approach)
@Test
void shouldCreateTaskWhenValidRequestProvided_OldWay() {
    // ... setup ...
    
    Task capturedTask = taskCaptor.getValue();
    assertEquals("Test Task", capturedTask.getTitle());
    assertEquals("Test Description", capturedTask.getDescription());
    assertEquals(TaskStatus.TODO, capturedTask.getStatus());
    assertEquals(TaskPriority.HIGH, capturedTask.getPriority());
    assertNotNull(capturedTask.getId());
    assertNotNull(capturedTask.getCreatedDate());
    assertNotNull(capturedTask.getUpdatedDate());
    assertEquals(capturedTask.getCreatedDate(), capturedTask.getUpdatedDate());
}
```

### When to Use Test Context Pattern

**RULE**: Apply Test Context Pattern based on object complexity and test requirements.

**Examples**:

✅ **Use Test Context Pattern When**:
- Domain object has **3+ fields** to validate
- Same test data is used across **multiple test methods**
- Object has **complex initialization** (many fields, nested objects)
- Tests involve **data transformations** (e.g., Request → Domain → Response)
- You need **scenario variations** (happy path, edge cases, error cases)

❌ **Don't Use Test Context Pattern When**:
- **Simple value objects** with 1-2 fields
- **One-off test data** used in a single test
- **Partial validation** is intentional (testing specific behavior)
- **Mock return values** that don't need validation

### Why ArgumentCaptor is Still Needed

**MANDATORY**: Always use ArgumentCaptor with Test Context Pattern to capture and validate actual method arguments.

**Examples**:

```java
// ✅ Good: ArgumentCaptor captures actual argument passed to mock
ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
verify(repository).insert(taskCaptor.capture());

Task capturedTask = taskCaptor.getValue();
Task expectedTask = context.createTaskWithGeneratedFields(
        capturedTask.getId(),
        capturedTask.getCreatedDate(),
        capturedTask.getUpdatedDate()
);

assertEquals(expectedTask, capturedTask);  // Tests controller logic

// ❌ Bad: Without ArgumentCaptor, can't validate what was actually passed
verify(repository).insert(any(Task.class));  // Only verifies method was called
// Can't test what the controller actually created and passed to repository
```

Reasons ArgumentCaptor is essential:
- **Tests Actual Logic**: Captures the object the controller creates and passes to the repository
- **Not Just Mock Behavior**: We're not testing what the mock returns, but what the controller actually does
- **Validates Transformations**: Ensures TaskRequest → Task mapping is correct
- **Captures Generated Values**: ID and timestamps are generated by the controller, not provided in the request

