---
type: testing-standards
technology: spring-boot, java
test-type: integration-testing
frameworks: junit5, testcontainers, spring-boot-test
audience: developers, ai-assistants
applies-to: backend, spring-boot-applications
---

# Spring Boot Integration Testing Standards

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
- **While writing tests**: Reference specific sections (REST API Testing, TestContainers, etc.)
- **During code review**: Check "Common Pitfalls to Avoid" → Verify against "AI Assistant Instructions"
- **For troubleshooting**: Use "Troubleshooting Common Issues" section for specific problems

---

## Quick Reference Patterns

### Standard Integration Test Setup

**CRITICAL RULE**: Integration tests must use `@SpringBootTest` or appropriate test slices with real infrastructure via TestContainers.

**Examples**:

```java
// ✅ Full application context with TestContainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderServiceIntegrationTest {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
        .withReuse(true);
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @Autowired
    private OrderService orderService;
    
    private List<String> testOrderIds = new ArrayList<>();
    
    @Test
    void shouldProduceExpectedResultWhenScenarioOccurs() {
        // Given: setup test data with unique identifiers
        String orderId = UUID.randomUUID().toString();
        testOrderIds.add(orderId);
        
        // When: execute the integration flow
        Order order = orderService.processOrder(orderDto);
        
        // Then: verify end-to-end behavior
        assertThat(orderRepository.findById(orderId)).isPresent();
    }
    
    @AfterEach
    void cleanup() {
        // Clean only test-specific data (never deleteAll())
        if (!testOrderIds.isEmpty()) {
            orderRepository.deleteAllById(testOrderIds);
            testOrderIds.clear();
        }
    }
}
```

### Essential Do's and Don'ts

**MANDATORY**: Follow these core practices for all integration tests.

**Examples**:

✅ **DO**:
- Use `@SpringBootTest` or appropriate test slices (`@DataMongoTest`, `@WebMvcTest`)
- Use TestContainers for real infrastructure (MongoDB, Redis, Kafka)
- Design for parallel execution with unique test data (UUIDs)
- Clean up only test-specific data in `@AfterEach` (never `deleteAll()`)
- Follow `should<Behavior>When<Condition>` naming convention
- Use `@DynamicPropertySource` for dynamic configuration
- Mock only external services outside your control (with WireMock)
- Test complete business workflows through multiple layers

❌ **DON'T**:
- Use embedded/in-memory databases (use real databases via TestContainers)
- Share state between tests or depend on test execution order
- Use `deleteAll()` in cleanup (breaks parallel execution)
- Test unit-level logic in integration tests
- Duplicate unit test coverage - focus on component interactions
- Use real external services (mock with WireMock)
- Use `Thread.sleep()` for async operations (use proper waiting)
- Hardcode configuration values (use properties and profiles)

---

## Key Principles

### Test Component Integration

**RULE**: Integration tests verify that multiple components work together correctly through complete workflows.

**Examples**:

Integration testing scope:
- **Controller → Service → Repository**: Complete data flow through all layers
- **Real database interactions**: Actual queries, persistence, transactions
- **Real Spring context**: Dependency injection, configuration, auto-wiring
- **REST API endpoints**: Real HTTP requests and responses
- **Message consumers/producers**: Kafka, RabbitMQ integration
- **Security flows**: Authentication, authorization, JWT validation
- **Scheduled jobs**: Background processes and cron tasks

### Parallel Execution by Design

**CRITICAL RULE**: Design all integration tests for parallel execution from day one - tests must run independently and concurrently.

**Examples**:

Parallel execution requirements:
- Use unique identifiers (UUID) for all test data
- No shared static state between tests
- No dependencies on test execution order
- Targeted cleanup (never `deleteAll()`)
- Filter query results by test-specific identifiers
- Use `RANDOM_PORT` for web tests
- Independent database records per test

### Complement Unit Tests

**MANDATORY**: Integration tests complement, not replace, unit tests - focus on component collaboration, not business logic details.

**Examples**:

Coverage strategy:
- **Unit tests**: Business logic, edge cases, validation, error handling (90%+ coverage)
- **Integration tests**: Multi-layer flows, database queries, API contracts (70-80% of integration points)
- **Avoid duplication**: Don't re-test edge cases already covered in unit tests
- **Focus on integration**: Test how components work together, not individual logic

---

## Test Type Decision

### When to Use Integration Tests

**MANDATORY**: Use integration tests for multi-layer interactions and real infrastructure validation.

**Examples**:

Use integration tests when:
- Testing controller → service → repository flow
- Verifying actual database queries and data persistence
- Testing REST API endpoints with real HTTP requests
- Validating security and authentication flows
- Testing message consumers/producers with real message brokers
- Verifying transaction rollback behavior
- Testing with real Spring context and dependency injection
- Testing scheduled jobs and background processes

### When to Use Unit Tests Instead

**RULE**: Reserve unit tests for isolated business logic with mocked dependencies.

**Examples**:

Use unit tests when:
- Testing business logic in isolation
- Testing edge cases and boundary conditions
- Testing error handling and validation logic
- Testing utility functions and helper methods
- Testing state management within a single class
- Fast feedback is critical (< 100ms per test)

---

## Framework and Tools

### Testing Framework Stack

**MANDATORY**: Use these frameworks for Spring Boot integration testing.

**Examples**:

Required frameworks:
- **Spring Boot Test**: `@SpringBootTest`, `@DataMongoTest`, `@WebMvcTest` for different scopes
- **TestContainers**: Real infrastructure (MongoDB, Redis, Kafka) in Docker containers
- **MockMvc / RestAssured**: REST API testing with actual HTTP
- **WireMock**: Mock external HTTP services outside your control
- **JUnit 5 & Hamcrest**: Testing framework and assertions

### File Naming and Location

**MANDATORY**: Follow consistent naming conventions to distinguish integration tests from unit tests.

**Examples**:

Test file naming:
- Integration test files must end with `IntegrationTest.java`
- Pattern: `<ComponentName>IntegrationTest.java`
- Examples: `UserRepositoryIntegrationTest.java`, `OrderProcessingIntegrationTest.java`

Location:
- Place integration tests in `src/test/java`
- Mirror the package structure of the code under test
- Keep integration tests separate from unit tests (naming convention helps build tools filter)

Test method naming:
- Follow pattern: `should<Behavior>When<Condition>`
- Examples:
  - `shouldCreateOrderInDatabaseWhenDataIsValid`
  - `shouldReturnEmptyOptionalWhenUserNotFoundByEmail`
  - `shouldRollBackTransactionWhenInsufficientFunds`

---

## Test Annotations and Contexts

### Choosing the Right Test Slice

**RULE**: Use the narrowest test slice that covers your testing scope for better performance.

**Examples**:

Test slice decision guide:
- **Full application flow (all layers)** → `@SpringBootTest`
- **Repository testing only** → `@DataMongoTest`
- **Controller testing with mocked services** → `@WebMvcTest`
- **Security testing** → `@SpringBootTest` with security config
- **Message consumer testing** → `@SpringBootTest` with TestContainers

### @SpringBootTest - Full Application Context

**RULE**: Use for testing complete application flows through all layers with real infrastructure.

**Examples**:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
class OrderProcessingIntegrationTest {
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
        .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    private List<String> testOrderIds = new ArrayList<>();
    
    @Test
    void shouldCreateOrderInDatabaseWhenDataIsValid() {
        // Given - Use unique identifier for parallel execution
        String orderId = UUID.randomUUID().toString();
        OrderDto orderDto = OrderDto.builder()
            .id(orderId)
            .customerId("customer-" + UUID.randomUUID())
            .items(List.of(
                new OrderItemDto("ITEM-001", 2),
                new OrderItemDto("ITEM-002", 1)
            ))
            .build();
        testOrderIds.add(orderId);
        
        // When
        Order order = orderService.processOrder(orderDto);
        
        // Then
        assertThat(order.getId(), is(notNullValue()));
        
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(savedOrder.getItems(), hasSize(2));
        assertThat(savedOrder.getStatus(), equalTo(OrderStatus.PENDING));
    }
    
    @AfterEach
    void cleanup() {
        if (!testOrderIds.isEmpty()) {
            orderRepository.deleteAllById(testOrderIds);
            testOrderIds.clear();
        }
    }
}
```

### @DataMongoTest - Repository Layer Testing

**RULE**: Use for testing MongoDB repositories with real database operations, without loading full application context.

**Examples**:

```java
@DataMongoTest
@Testcontainers
class UserRepositoryIntegrationTest {
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
        .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
    
    @Autowired
    private UserRepository userRepository;
    
    private List<String> testUserIds = new ArrayList<>();
    
    @Test
    void shouldReturnUserWhenFoundByEmail() {
        // Given - Use unique email for parallel execution
        String uniqueEmail = "john.doe." + UUID.randomUUID() + "@example.com";
        User user = new User(uniqueEmail, "John", "Doe");
        User saved = userRepository.save(user);
        testUserIds.add(saved.getId());
        
        // When
        Optional<User> found = userRepository.findByEmail(uniqueEmail);
        
        // Then
        assertThat(found.isPresent(), is(true));
        assertThat(found.get().getEmail(), equalTo(uniqueEmail));
        assertThat(found.get().getFirstName(), equalTo("John"));
    }
    
    @Test
    void shouldReturnOnlyActiveUsersWhenFilteringByActive() {
        // Given - Use unique identifiers
        String uniqueId = UUID.randomUUID().toString();
        User activeUser1 = new User("active1." + uniqueId + "@example.com", "Active", "One");
        activeUser1.setActive(true);
        User activeUser2 = new User("active2." + uniqueId + "@example.com", "Active", "Two");
        activeUser2.setActive(true);
        User inactiveUser = new User("inactive." + uniqueId + "@example.com", "Inactive", "User");
        inactiveUser.setActive(false);
        
        List<User> savedUsers = userRepository.saveAll(List.of(activeUser1, activeUser2, inactiveUser));
        savedUsers.forEach(u -> testUserIds.add(u.getId()));
        
        // When
        List<User> activeUsers = userRepository.findByActiveTrue();
        
        // Then - Filter to only our test users
        List<User> testActiveUsers = activeUsers.stream()
            .filter(u -> u.getEmail().contains(uniqueId))
            .collect(Collectors.toList());
        
        assertThat(testActiveUsers, hasSize(2));
        assertThat(testActiveUsers.stream().map(User::getEmail).collect(Collectors.toList()),
            containsInAnyOrder("active1." + uniqueId + "@example.com",
                              "active2." + uniqueId + "@example.com"));
    }
    
    @AfterEach
    void cleanup() {
        if (!testUserIds.isEmpty()) {
            userRepository.deleteAllById(testUserIds);
            testUserIds.clear();
        }
    }
}
```

### @WebMvcTest - Controller Layer Testing

**RULE**: Use for testing REST controllers with MockMvc, mocking the service layer for focused controller testing.

**Examples**:

```java
@WebMvcTest(UserController.class)
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldReturnCreatedStatusWhenUserDataIsValid() throws Exception {
        // Given
        UserDto userDto = new UserDto("john.doe@example.com", "password123");
        User user = new User(1L, "john.doe@example.com", "John", "Doe");
        
        when(userService.createUser(any(UserDto.class))).thenReturn(user);
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"));
        
        verify(userService).createUser(any(UserDto.class));
    }
    
    @Test
    void shouldReturnNotFoundWhenUserNotFoundById() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.findById(userId))
            .thenThrow(new UserNotFoundException("User not found"));
        
        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User not found"));
    }
}
```

---

## TestContainers Integration

### Why TestContainers

**CRITICAL RULE**: Always use TestContainers with real infrastructure - never use embedded or in-memory databases for integration tests.

**Examples**:

Benefits of TestContainers:
- **Real infrastructure**: Test with actual MongoDB, Redis, Kafka (not embedded versions)
- **Production parity**: Same database version and features as production
- **Isolation**: Each test run gets fresh containers
- **Reproducibility**: Consistent environment across developers and CI/CD
- **Docker-based**: Automatically manages container lifecycle

### MongoDB Testing with TestContainers

**MANDATORY**: Use real MongoDB containers for all database integration tests.

**Examples**:

```java
@SpringBootTest
@Testcontainers
class ProductRepositoryIntegrationTest {
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
        .withReuse(true);  // Reuse container across tests for performance
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private List<String> testProductIds = new ArrayList<>();
    
    @Test
    void testDatabaseConnection() {
        // Given
        assertThat(mongodb.isRunning(), is(true));
        
        // When
        String databaseName = mongoTemplate.getDb().getName();
        
        // Then
        assertThat(databaseName, is(notNullValue()));
    }
    
    @Test
    void shouldReturnFilteredListWhenFindingByCategory() {
        // Given
        String productId1 = UUID.randomUUID().toString();
        String productId2 = UUID.randomUUID().toString();
        String category = "Electronics-" + UUID.randomUUID();
        
        productRepository.save(Product.builder()
            .id(productId1)
            .name("Product 1")
            .category(category)
            .build());
        productRepository.save(Product.builder()
            .id(productId2)
            .name("Product 2")
            .category(category)
            .build());
        
        testProductIds.addAll(Arrays.asList(productId1, productId2));
        
        // When
        List<Product> results = productRepository.findByCategory(category);
        
        // Then
        assertThat(results, hasSize(2));
        assertThat(results.stream().map(Product::getCategory).collect(Collectors.toList()),
            everyItem(equalTo(category)));
    }
    
    @Test
    void testCrudOperations() {
        // Given
        Product product = new Product("test-product@example.com", "Test Product", "Category");
        
        // When - Create
        Product saved = productRepository.save(product);
        testProductIds.add(saved.getId());
        assertThat(saved.getId(), is(notNullValue()));
        
        // When - Read
        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found.isPresent(), is(true));
        
        // When - Update
        found.get().setName("Updated Product");
        productRepository.save(found.get());
        
        // When - Delete
        productRepository.deleteById(saved.getId());
        testProductIds.remove(saved.getId());
        assertThat(productRepository.findById(saved.getId()).isEmpty(), is(true));
    }
    
    @AfterEach
    void cleanup() {
        if (!testProductIds.isEmpty()) {
            productRepository.deleteAllById(testProductIds);
            testProductIds.clear();
        }
    }
}
```

### Redis Testing with TestContainers

**RULE**: Use Redis containers for caching and session management integration tests.

**Examples**:

```java
@SpringBootTest
@Testcontainers
class CacheIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379)
        .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private List<String> testCacheKeys = new ArrayList<>();
    
    @Test
    void shouldStoreInRedisWhenCachingValueFirstTime() {
        // Given
        String key = "cache-key-" + UUID.randomUUID();
        String value = "cached-value";
        testCacheKeys.add(key);
        
        // When
        redisTemplate.opsForValue().set(key, value);
        
        // Then
        String cachedValue = redisTemplate.opsForValue().get(key);
        assertThat(cachedValue, equalTo(value));
    }
    
    @Test
    void shouldReturnCachedValueWhenCallingSecondTime() {
        // Given
        Long userId = 1L;
        
        // When - First call
        User user1 = userService.findById(userId);
        
        // When - Second call (should be from cache)
        User user2 = userService.findById(userId);
        
        // Then
        assertThat(user1, is(notNullValue()));
        assertThat(user2, is(notNullValue()));
        assertThat(user1, is(sameInstance(user2)));  // Same object instance from cache
    }
    
    @AfterEach
    void cleanup() {
        testCacheKeys.forEach(key -> redisTemplate.delete(key));
        testCacheKeys.clear();
    }
}
```

### Kafka Testing with TestContainers

**RULE**: Use Kafka containers for message consumer/producer integration tests.

**Examples**:

```java
@SpringBootTest
@Testcontainers
class KafkaConsumerIntegrationTest {
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    ).withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    }
    
    @Autowired
    private KafkaTemplate<String, OrderMessage> kafkaTemplate;
    
    @SpyBean
    private OrderConsumer orderConsumer;
    
    private static final String TOPIC = "orders-topic";
    
    @Test
    void consumeOrder_ProcessesMessageSuccessfully() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderMessage message = new OrderMessage(orderId, "John Doe", 100.00);
        
        // When
        kafkaTemplate.send(TOPIC, message.getOrderId(), message).get();
        
        // Then - Wait for async processing
        verify(orderConsumer, timeout(5000)).processOrder(
            argThat(order -> order.getOrderId().equals(orderId)));
    }
    
    @Test
    void consumeMultipleOrders_ProcessesAllMessages() throws Exception {
        // Given
        List<OrderMessage> messages = List.of(
            new OrderMessage("ORDER-001", "John Doe", 100.00),
            new OrderMessage("ORDER-002", "Jane Smith", 200.00),
            new OrderMessage("ORDER-003", "Bob Johnson", 150.00)
        );
        
        // When
        for (OrderMessage message : messages) {
            kafkaTemplate.send(TOPIC, message.getOrderId(), message).get();
        }
        
        // Then
        verify(orderConsumer, timeout(5000).times(3))
            .processOrder(any(OrderMessage.class));
    }
}
```

---

## Testing REST APIs

### MockMvc vs RestAssured

**RULE**: Choose the REST testing tool based on your testing needs and team preference.

**Examples**:

Decision guide:
- **MockMvc**: Testing controllers without starting full HTTP server, faster, good for controller layer tests
- **RestAssured**: BDD-style API testing with full HTTP, better for full integration tests, more readable

### REST API Testing with MockMvc

**RULE**: Use MockMvc for controller integration tests with Spring's test framework.

**Examples**:

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserApiIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    private List<String> testUserIds = new ArrayList<>();
    
    @Test
    void shouldReturnCreatedUserWhenDataIsValid() throws Exception {
        // Given - Use unique identifier for parallel execution
        String uniqueEmail = "test." + UUID.randomUUID() + "@example.com";
        UserDto userDto = UserDto.builder()
            .email(uniqueEmail)
            .name("Test User")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(uniqueEmail))
            .andExpect(jsonPath("$.name").value("Test User"))
            .andExpect(jsonPath("$.id").exists());
    }
    
    @Test
    void shouldReturnUserWhenUserExists() throws Exception {
        // Given
        String userId = UUID.randomUUID().toString();
        User user = userRepository.save(User.builder()
            .id(userId)
            .email("test@example.com")
            .name("Test User")
            .build());
        testUserIds.add(userId);
        
        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value(user.getEmail()));
    }
    
    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        // Given
        UserDto invalidDto = UserDto.builder()
            .email("invalid-email")
            .name("Test User")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldWorkCorrectlyWhenExecutingFullUserLifecycle() throws Exception {
        // Create user - Use unique identifier for parallel execution
        String uniqueEmail = "john.doe." + UUID.randomUUID() + "@example.com";
        UserDto createDto = new UserDto(uniqueEmail, "password123");
        
        String createResponse = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value(uniqueEmail))
            .andReturn().getResponse().getContentAsString();
        
        Long userId = objectMapper.readTree(createResponse).get("id").asLong();
        testUserIds.add(userId.toString());
        
        // Update user
        UserDto updateDto = new UserDto("john.updated@example.com", null);
        
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("john.updated@example.com"));
        
        // Delete user
        mockMvc.perform(delete("/api/users/{id}", userId))
            .andExpect(status().isNoContent());
        testUserIds.remove(userId.toString());
        
        // Verify deletion
        mockMvc.perform(get("/api/users/{id}", userId))
            .andExpect(status().isNotFound());
    }
    
    @AfterEach
    void cleanup() {
        if (!testUserIds.isEmpty()) {
            userRepository.deleteAllById(testUserIds);
            testUserIds.clear();
        }
    }
}
```

### REST API Testing with RestAssured

**RULE**: Use RestAssured for BDD-style API integration tests with more readable syntax.

**Examples**:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderApiRestAssuredTest {
    
    @LocalServerPort
    private int port;
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
    
    @Autowired
    private OrderRepository orderRepository;
    
    private List<String> testOrderIds = new ArrayList<>();
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }
    
    @Test
    void shouldReturnCreatedUserWhenDataIsValid() {
        // Given - Use unique identifier for parallel execution
        String uniqueEmail = "john.doe." + UUID.randomUUID() + "@example.com";
        UserDto userDto = new UserDto(uniqueEmail, "password123");
        
        given()
            .contentType(ContentType.JSON)
            .body(userDto)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("email", equalTo(uniqueEmail))
            .body("id", notNullValue());
    }
    
    @Test
    void shouldReturnCreatedOrderWhenDataIsValid() {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderDto orderDto = OrderDto.builder()
            .customerId("customer-" + UUID.randomUUID())
            .totalAmount(BigDecimal.valueOf(150.00))
            .build();
        testOrderIds.add(orderId);
        
        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(orderDto)
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
            .body("customerId", equalTo(orderDto.getCustomerId()))
            .body("totalAmount", equalTo(150.00f))
            .body("id", notNullValue());
    }
    
    @Test
    void shouldReturnOrderWhenOrderExists() {
        // Given
        String orderId = UUID.randomUUID().toString();
        Order order = orderRepository.save(Order.builder()
            .id(orderId)
            .customerId("customer-123")
            .totalAmount(BigDecimal.valueOf(200.00))
            .build());
        testOrderIds.add(orderId);
        
        // When & Then
        given()
            .pathParam("id", orderId)
        .when()
            .get("/orders/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(orderId))
            .body("totalAmount", equalTo(200.00f));
    }
    
    @AfterEach
    void cleanup() {
        if (!testOrderIds.isEmpty()) {
            orderRepository.deleteAllById(testOrderIds);
            testOrderIds.clear();
        }
    }
}
```

---

## Mocking External Services

### When to Mock External Services

**MANDATORY**: Mock external services outside your control - never call real external APIs in integration tests.

**Examples**:

Mock these external services:
- Third-party REST APIs (payment gateways, shipping providers)
- External authentication services (OAuth providers)
- Weather services, geolocation APIs
- Any service outside your organization's control
- Services with rate limits or costs per call

Do NOT mock:
- Your own microservices (use real services or TestContainers)
- Database connections (use TestContainers)
- Message brokers (use TestContainers)
- Cache systems (use TestContainers)

### External Service Mocking with WireMock

**RULE**: Use WireMock to mock external HTTP services with realistic responses and scenarios.

**Examples**:

```java
@SpringBootTest
@AutoConfigureWireMock(port = 0)
class ExternalApiIntegrationTest {
    
    @Value("${wiremock.server.port}")
    private int wireMockPort;
    
    @Autowired
    private PaymentGatewayClient paymentClient;
    
    @Test
    void shouldReturnConfirmationWhenExternalApiSucceeds() {
        // Given - Mock external API
        String transactionId = UUID.randomUUID().toString();
        stubFor(post(urlEqualTo("/api/payments"))
            .withRequestBody(matchingJsonPath("$.amount"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "transactionId": "%s",
                        "status": "SUCCESS"
                    }
                    """.formatted(transactionId))));
        
        PaymentRequest request = new PaymentRequest(100.00, "USD");
        
        // When
        PaymentResponse response = paymentClient.processPayment(request);
        
        // Then
        assertThat(response.getTransactionId(), equalTo(transactionId));
        assertThat(response.getStatus(), equalTo("SUCCESS"));
        
        // Verify external API was called
        verify(postRequestedFor(urlEqualTo("/api/payments"))
            .withRequestBody(matchingJsonPath("$.amount", equalTo("100.0"))));
    }
    
    @Test
    void shouldReturnFailureWhenCardIsDeclined() {
        // Given - Mock external API failure
        stubFor(post(urlEqualTo("/api/payments"))
            .willReturn(aResponse()
                .withStatus(402)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "status": "DECLINED",
                        "errorCode": "INSUFFICIENT_FUNDS"
                    }
                    """)));
        
        PaymentRequest request = new PaymentRequest(100.00, "USD");
        
        // When
        PaymentResponse result = paymentClient.processPayment(request);
        
        // Then
        assertThat(result.getStatus(), equalTo("DECLINED"));
        assertThat(result.getErrorCode(), equalTo("INSUFFICIENT_FUNDS"));
    }
}
```

---

## Testing Security

### Security Integration Testing

**RULE**: Test authentication and authorization flows with real Spring Security configuration.

**Examples**:

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldReturnUnauthorizedWhenAccessingProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldReturnOkWhenAccessingProtectedEndpointWithUserRole() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldReturnForbiddenWhenAccessingAdminEndpointWithUserRole() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnOkWhenAccessingAdminEndpointWithAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk());
    }
    
    @Test
    void shouldReturnJwtTokenWhenLoginWithValidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
            .username("admin@example.com")
            .password("securePassword")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
```

---

## Testing Transactions

### MongoDB Transaction Testing

**RULE**: Test transaction rollback behavior with MongoDB replica set configuration.

**Examples**:

```java
@SpringBootTest
@Testcontainers
class TransactionIntegrationTest {
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
        .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private AccountRepository accountRepository;
    
    private List<String> testAccountIds = new ArrayList<>();
    
    @Test
    void shouldUpdateBothAccountsWhenTransferIsSuccessful() {
        // Given - Use unique identifiers for parallel execution
        String uniqueId = UUID.randomUUID().toString();
        Account fromAccount = accountRepository.save(
            new Account("ACC-" + uniqueId + "-FROM", 1000.00));
        Account toAccount = accountRepository.save(
            new Account("ACC-" + uniqueId + "-TO", 500.00));
        testAccountIds.add(fromAccount.getId());
        testAccountIds.add(toAccount.getId());
        
        // When
        accountService.transferMoney(fromAccount.getId(), toAccount.getId(), 200.00);
        
        // Then
        Account updatedFrom = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account updatedTo = accountRepository.findById(toAccount.getId()).orElseThrow();
        
        assertThat(updatedFrom.getBalance(), equalTo(800.00));
        assertThat(updatedTo.getBalance(), equalTo(700.00));
    }
    
    @Test
    void shouldRollBackTransactionWhenInsufficientFunds() {
        // Given
        String uniqueId = UUID.randomUUID().toString();
        Account fromAccount = accountRepository.save(
            new Account("ACC-" + uniqueId + "-FROM", 100.00));
        Account toAccount = accountRepository.save(
            new Account("ACC-" + uniqueId + "-TO", 500.00));
        testAccountIds.add(fromAccount.getId());
        testAccountIds.add(toAccount.getId());
        
        // When & Then
        assertThrows(InsufficientFundsException.class, () ->
            accountService.transferMoney(fromAccount.getId(), toAccount.getId(), 200.00));
        
        // Verify no changes due to rollback
        Account fromAfter = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account toAfter = accountRepository.findById(toAccount.getId()).orElseThrow();
        
        assertThat(fromAfter.getBalance(), equalTo(100.00));
        assertThat(toAfter.getBalance(), equalTo(500.00));
    }
    
    @AfterEach
    void cleanup() {
        if (!testAccountIds.isEmpty()) {
            accountRepository.deleteAllById(testAccountIds);
            testAccountIds.clear();
        }
    }
}
```

---

## Additional Testing Patterns

### Testing Scheduled Tasks

**RULE**: Test scheduled jobs by manually triggering them and verifying results.

**Examples**:

```java
@SpringBootTest
class ScheduledTaskIntegrationTest {
    
    @Autowired
    private ReportGenerationScheduler scheduler;
    
    @Autowired
    private ReportRepository reportRepository;
    
    @SpyBean
    private EmailService emailService;
    
    private List<String> testReportIds = new ArrayList<>();
    
    @Test
    void shouldCreateReportAndSendEmailWhenDailyReportExecutesSuccessfully() {
        // Given - setup test data
        String reportId = UUID.randomUUID().toString();
        testReportIds.add(reportId);
        
        // When - manually trigger scheduled method
        scheduler.generateDailyReport();
        
        // Then - verify report was created
        Optional<Report> report = reportRepository.findByType("DAILY");
        assertThat(report).isPresent();
        
        // Verify email was sent
        verify(emailService).sendReport(any(Report.class));
    }
    
    @AfterEach
    void cleanup() {
        if (!testReportIds.isEmpty()) {
            reportRepository.deleteAllById(testReportIds);
            testReportIds.clear();
        }
    }
}
```

### Using Test Profiles

**RULE**: Use `@ActiveProfiles` with custom configuration for test-specific settings.

**Examples**:

```java
@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
class DataMigrationIntegrationTest {
    // Test will use application-integration-test.yml configuration
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    // Test implementation...
}
```

Configuration file `application-integration-test.yml`:
```yaml
spring:
  data:
    mongodb:
      database: test-database
  cache:
    type: none  # Disable caching in tests
logging:
  level:
    org.springframework: INFO
    com.myapp: DEBUG
```

---

## Performance and Optimization

### Parallel Test Execution

**CRITICAL RULE**: Design for parallel execution from day one - integration tests must run concurrently for reasonable CI/CD times.

**Examples**:

Parallel execution requirements:
- **Use unique identifiers**: Generate UUIDs for all test data to avoid collisions
- **No shared state**: Each test is completely independent
- **Targeted cleanup**: Delete only test-specific data (never `deleteAll()`)
- **Filter queries**: Query only test-specific data using unique identifiers
- **Random ports**: Use `RANDOM_PORT` for web tests to avoid port conflicts

Maven Surefire parallel configuration:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
        <perCoreThreadCount>true</perCoreThreadCount>
    </configuration>
</plugin>
```

### Container Reuse and Optimization

**RULE**: Optimize TestContainers performance with reuse and proper configuration.

**Examples**:

Container optimization strategies:
```java
// ✅ Reuse containers across tests
@Container
static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
    .withReuse(true);  // Reuse container across test runs

// ✅ Share containers with static @Container
static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

// ✅ Use @DirtiesContext sparingly (only when context must be refreshed)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
```

### Test Slice Selection

**RULE**: Choose the narrowest test slice for better performance.

**Examples**:

Performance hierarchy (fastest to slowest):
1. **`@DataMongoTest`**: Only repository layer, minimal context
2. **`@WebMvcTest`**: Only controller layer, mocked services
3. **`@SpringBootTest`**: Full application context, all beans

Decision guide:
- Testing repositories only → Use `@DataMongoTest`
- Testing controllers with mocked services → Use `@WebMvcTest`
- Testing complete end-to-end flow → Use `@SpringBootTest`

### Performance Targets

**RULE**: Monitor and optimize integration test performance to maintain reasonable CI/CD times.

**Examples**:

Performance targets:
- **Target**: < 5 seconds per integration test
- **Maximum**: 10 seconds per integration test
- **Total suite**: Should complete in < 10 minutes with parallel execution

Optimization techniques:
- Use MongoDB bulk operations for test data setup
- Implement pagination for large result sets
- Create appropriate MongoDB indexes for test queries
- Mock slow external services with WireMock
- Use container reuse to avoid startup overhead

---

## Common Pitfalls to Avoid

### Understanding Anti-Patterns

**CRITICAL RULE**: Avoid these common mistakes that break parallel execution and test reliability.

**Examples**:

Review these pitfalls regularly:
1. Shared test data without unique identifiers
2. Global cleanup with `deleteAll()`
3. Static waits with `Thread.sleep()`
4. Testing framework logic instead of business logic
5. Using embedded databases instead of TestContainers
6. Depending on test execution order
7. Not cleaning up test data
8. Hardcoding ports and configuration
9. Duplicating unit test coverage
10. Testing external services directly

---

### 1. Shared Test Data Without Unique Identifiers

❌ **DON'T**: Use hardcoded values that will conflict in parallel execution.

```java
// ❌ Bad: Hardcoded email shared across tests
@Test
void testUserCreation() {
    String email = "test@example.com";  // Will conflict in parallel execution!
    User user = userService.createUser(email);
    assertThat(user.getEmail(), equalTo(email));
}

@Test
void testUserDeletion() {
    String email = "test@example.com";  // Same email - collision!
    User user = userService.createUser(email);
    userService.deleteUser(user.getId());
}
```

✅ **DO**: Generate unique identifiers for all test data to enable parallel execution.

```java
// ✅ Good: Unique identifier per test
@Test
void testUserCreation() {
    String email = "test." + UUID.randomUUID() + "@example.com";
    User user = userService.createUser(email);
    assertThat(user.getEmail(), equalTo(email));
}

@Test
void testUserDeletion() {
    String email = "test." + UUID.randomUUID() + "@example.com";
    User user = userService.createUser(email);
    userService.deleteUser(user.getId());
}
```

### 2. Global Cleanup with deleteAll()

❌ **DON'T**: Delete all data - this breaks parallel test execution.

```java
// ❌ Bad: Deletes all data (breaks parallel tests)
@AfterEach
void cleanup() {
    userRepository.deleteAll();  // Deletes other tests' data!
    orderRepository.deleteAll();
}
```

✅ **DO**: Track and delete only test-specific data created by each test.

```java
// ✅ Good: Targeted cleanup of test-specific data
private List<String> testUserIds = new ArrayList<>();
private List<String> testOrderIds = new ArrayList<>();

@Test
void testOrderCreation() {
    String userId = UUID.randomUUID().toString();
    String orderId = UUID.randomUUID().toString();
    testUserIds.add(userId);
    testOrderIds.add(orderId);
    
    // Test logic...
}

@AfterEach
void cleanup() {
    if (!testOrderIds.isEmpty()) {
        orderRepository.deleteAllById(testOrderIds);
        testOrderIds.clear();
    }
    if (!testUserIds.isEmpty()) {
        userRepository.deleteAllById(testUserIds);
        testUserIds.clear();
    }
}
```

### 3. Static Waits with Thread.sleep()

❌ **DON'T**: Use arbitrary sleep times - they're slow and flaky.

```java
// ❌ Bad: Arbitrary wait time
kafkaTemplate.send(topic, message);
Thread.sleep(3000);  // Flaky and slow!
verify(consumer).processMessage(any());
```

✅ **DO**: Use conditional waits with timeouts from test frameworks.

```java
// ✅ Good: Wait for actual condition with timeout
kafkaTemplate.send(topic, message);
verify(consumer, timeout(5000)).processMessage(any());

// ✅ Good: Use Awaitility for complex conditions
await().atMost(5, TimeUnit.SECONDS)
    .until(() -> orderRepository.findById(orderId).isPresent());
```

### 4. Testing Framework Logic

❌ **DON'T**: Test that Spring Boot or frameworks work - trust they do.

```java
// ❌ Bad: Testing Spring Boot's auto-configuration
@Test
void testSpringContextLoads() {
    assertThat(applicationContext).isNotNull();
}

// ❌ Bad: Testing that autowiring works
@Test
void testServiceAutowired() {
    assertThat(orderService).isNotNull();
}
```

✅ **DO**: Test your business workflows and integration logic.

```java
// ✅ Good: Test your business workflow
@Test
void shouldCreateOrderInDatabaseWhenProcessingWithValidData() {
    // Given
    String orderId = UUID.randomUUID().toString();
    OrderDto orderDto = createTestOrderDto(orderId);
    testOrderIds.add(orderId);
    
    // When
    Order order = orderService.processOrder(orderDto);
    
    // Then
    Optional<Order> savedOrder = orderRepository.findById(orderId);
    assertThat(savedOrder).isPresent();
    assertThat(savedOrder.get().getStatus(), equalTo(OrderStatus.CREATED));
}
```

### 5. Using Embedded Databases

❌ **DON'T**: Use embedded or in-memory databases - they don't match production.

```java
// ❌ Bad: Using embedded MongoDB (not production-like)
@DataMongoTest
class UserRepositoryTest {
    // Using embedded MongoDB from Flapdoodle
    @Autowired
    private UserRepository userRepository;
}
```

✅ **DO**: Always use TestContainers with real database versions.

```java
// ✅ Good: Using real MongoDB via TestContainers
@DataMongoTest
@Testcontainers
class UserRepositoryIntegrationTest {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @Autowired
    private UserRepository userRepository;
}
```

### 6. Depending on Test Execution Order

❌ **DON'T**: Create dependencies between tests or assume execution order.

```java
// ❌ Bad: Tests depend on execution order
private static String sharedUserId;

@Test
void test1_CreateUser() {
    User user = userService.createUser(userDto);
    sharedUserId = user.getId();  // Sharing state!
}

@Test
void test2_UpdateUser() {
    User user = userRepository.findById(sharedUserId).get();  // Depends on test1!
    user.setName("Updated Name");
    userService.updateUser(user);
}
```

✅ **DO**: Make each test completely independent with its own setup.

```java
// ✅ Good: Independent tests with own setup
private List<String> testUserIds = new ArrayList<>();

@Test
void shouldSaveUserToDatabaseWhenCreatingWithValidData() {
    // Given
    String userId = UUID.randomUUID().toString();
    UserDto userDto = createTestUserDto(userId);
    testUserIds.add(userId);
    
    // When
    User user = userService.createUser(userDto);
    
    // Then
    assertThat(userRepository.findById(userId)).isPresent();
}

@Test
void shouldUpdateSuccessfullyWhenUserExists() {
    // Given - create own test data
    String userId = UUID.randomUUID().toString();
    User user = createAndSaveTestUser(userId);
    testUserIds.add(userId);
    
    // When
    user.setName("Updated Name");
    userService.updateUser(user);
    
    // Then
    User updatedUser = userRepository.findById(userId).get();
    assertThat(updatedUser.getName(), equalTo("Updated Name"));
}

@AfterEach
void cleanup() {
    if (!testUserIds.isEmpty()) {
        userRepository.deleteAllById(testUserIds);
        testUserIds.clear();
    }
}
```

### 7. Not Cleaning Up Test Data

❌ **DON'T**: Leave test data in the database after tests complete.

```java
// ❌ Bad: No cleanup - data accumulates
@Test
void testUserCreation() {
    User user = userService.createUser(userDto);
    assertThat(user.getId()).isNotNull();
    // No cleanup!
}
```

✅ **DO**: Always clean up test-specific data in `@AfterEach`.

```java
// ✅ Good: Proper cleanup
private List<String> testUserIds = new ArrayList<>();

@Test
void shouldSaveUserToDatabaseWhenCreatingWithValidData() {
    String userId = UUID.randomUUID().toString();
    testUserIds.add(userId);
    
    User user = userService.createUser(createTestUserDto(userId));
    assertThat(user.getId()).isNotNull();
}

@AfterEach
void cleanup() {
    if (!testUserIds.isEmpty()) {
        userRepository.deleteAllById(testUserIds);
        testUserIds.clear();
    }
}
```

### 8. Hardcoding Ports and Configuration

❌ **DON'T**: Hardcode ports or configuration values - causes conflicts in parallel execution.

```java
// ❌ Bad: Hardcoded port
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = "server.port=8080"  // Conflicts with other tests!
)
class ApiIntegrationTest {
}
```

✅ **DO**: Use random ports and dynamic configuration.

```java
// ✅ Good: Random port and dynamic configuration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApiIntegrationTest {
    
    @LocalServerPort
    private int port;  // Dynamically assigned
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
}
```

### 9. Duplicating Unit Test Coverage

❌ **DON'T**: Re-test business logic edge cases already covered by unit tests.

```java
// ❌ Bad: Testing validation logic in integration test
@Test
void shouldThrowValidationExceptionWhenEmailIsEmpty() {
    UserDto invalidDto = UserDto.builder().email("").build();
    assertThrows(ValidationException.class,
        () -> userService.createUser(invalidDto));
}

// ❌ Bad: Testing null handling
@Test
void shouldThrowValidationExceptionWhenNameIsNull() {
    UserDto invalidDto = UserDto.builder().name(null).build();
    assertThrows(ValidationException.class,
        () -> userService.createUser(invalidDto));
}
```

✅ **DO**: Focus on multi-layer integration, let unit tests handle edge cases.

```java
// ✅ Good: Testing end-to-end flow through all layers
@Test
void shouldSaveUserAndReturnWithIdWhenCreatingWithValidData() {
    // Given
    String email = "test." + UUID.randomUUID() + "@example.com";
    UserDto userDto = UserDto.builder()
        .email(email)
        .name("Test User")
        .build();
    
    // When - test full integration: controller → service → repository
    User user = userService.createUser(userDto);
    
    // Then - verify end-to-end persistence
    Optional<User> savedUser = userRepository.findById(user.getId());
    assertThat(savedUser).isPresent();
    assertThat(savedUser.get().getEmail(), equalTo(email));
}
```

### 10. Testing External Services Directly

❌ **DON'T**: Call real external APIs - they're slow, costly, and unreliable in tests.

```java
// ❌ Bad: Calling real payment gateway
@Test
void shouldChargeCustomerWhenCardIsValid() {
    // This calls the real Stripe API!
    PaymentResult result = paymentService.charge(
        "real_api_key",
        cardNumber,
        amount
    );
    assertThat(result.isSuccess()).isTrue();
}
```

✅ **DO**: Mock external services with WireMock for predictable, fast tests.

```java
// ✅ Good: Mocking external service with WireMock
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

@DynamicPropertySource
static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("payment.api.url", wireMock::baseUrl);
}

@Test
void shouldReturnSuccessWhenProcessingPaymentWithValidCard() {
    // Given - mock external API response
    wireMock.stubFor(post(urlEqualTo("/api/charge"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"status\": \"success\"}")));
    
    // When
    PaymentResult result = paymentService.charge(cardNumber, amount);
    
    // Then
    assertThat(result.isSuccess()).isTrue();
}
```

---

## Troubleshooting Common Issues

### Port Conflicts

**RULE**: Use random ports and avoid hardcoded port numbers to prevent conflicts.

**Examples**:

Problem: Tests fail with "Address already in use"

Solution:
```java
// ✅ Use RANDOM_PORT
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {
    @LocalServerPort
    private int port;  // Dynamically assigned
}

// ✅ TestContainers automatically assigns random ports
@Container
static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
```

### Database State Issues

**RULE**: Ensure proper cleanup and test independence to avoid state pollution.

**Examples**:

Problem: Tests pass individually but fail when run together

Solution:
- Use unique identifiers (UUID) for all test data
- Implement targeted cleanup in `@AfterEach`
- Never use `deleteAll()` - only delete test-specific data
- Use `@Transactional` for automatic rollback (where applicable)

### TestContainers Issues

**RULE**: Verify Docker is running and properly configured.

**Examples**:

Problem: "Could not find a valid Docker environment"

Solutions:
- Ensure Docker daemon is running
- Check Docker has sufficient resources (memory, disk)
- Verify TestContainers can access Docker socket
- Use `.withReuse(true)` to speed up tests

### Flaky Tests

**RULE**: Eliminate timing-based flakiness with proper synchronization.

**Examples**:

Problem: Tests sometimes pass, sometimes fail

Solutions:
- Never use `Thread.sleep()` - use `verify(mock, timeout())` or Awaitility
- Ensure tests are independent (no shared state)
- Use unique test data to avoid conflicts
- Check for race conditions in async operations
- Use proper waiting mechanisms for async processing

---

## AI Assistant Instructions

### Before Generating Any Test Code

**CRITICAL RULE**: AI assistants must complete this preparation before generating any integration test code.

**Examples**:

1. **Read the Pre-Implementation Checklist** at the end of this document
2. **Review "Common Pitfalls to Avoid"** section to avoid anti-patterns
3. **Verify test type decision** - confirm integration test is appropriate (not unit test)
4. **Use "Quick Reference Patterns"** section for standard templates
5. **Check parallel execution requirements** - ensure unique test data with UUIDs

### Mandatory Practices for AI-Generated Tests

**CRITICAL RULE**: Every integration test generated by AI assistants must follow these practices without exception.

**Examples**:

1. **Always use TestContainers**: Never use embedded or in-memory databases. Always use real infrastructure via TestContainers.

2. **Design for parallel execution**: Use UUID for all test data identifiers. Never use hardcoded values that could conflict.

3. **Implement targeted cleanup**: Track test-specific data and clean up in `@AfterEach`. Never use `deleteAll()`.

4. **Follow naming conventions**: Use `<ComponentName>IntegrationTest.java` for class names and `should<Behavior>When<Condition>` for test method names.

5. **Use appropriate test slices**: Choose `@SpringBootTest`, `@DataMongoTest`, or `@WebMvcTest` based on scope.

6. **Mock external services only**: Use WireMock for external APIs. Use real implementations for internal services, databases, and message brokers.

7. **Use @DynamicPropertySource**: Configure TestContainers with dynamic properties, not hardcoded values.

8. **Test complete workflows**: Focus on multi-layer integration, not business logic details (that's for unit tests).

9. **No static waits**: Use `verify(mock, timeout())` or Awaitility instead of `Thread.sleep()`.

10. **Random ports**: Always use `RANDOM_PORT` for web tests to avoid port conflicts.

11. **Clean test structure**: Use Given-When-Then comments for clarity.

12. **Proper assertions**: Use Hamcrest matchers for readable assertions.

13. **No framework testing**: Don't test that Spring Boot works. Test your integration logic.

14. **Track test data**: Maintain lists of test entity IDs for cleanup.

15. **Use test profiles**: Apply `@ActiveProfiles("integration-test")` when using test-specific configuration.

### Quick Decision Trees for AI Assistants

**RULE**: Use these decision trees for quick answers when generating integration tests.

**Examples**:

**Q: What annotation should I use?**
- Full application flow (all layers) → `@SpringBootTest`
- Repository testing only → `@DataMongoTest`
- Controller with mocked services → `@WebMvcTest`
- Security testing → `@SpringBootTest` with security config

**Q: Should I use TestContainers or embedded database?**
- Always use TestContainers with real databases (MongoDB, Redis, Kafka)
- Never use embedded/in-memory databases for integration tests

**Q: Should I mock this dependency?**
- External third-party service (payment gateway, weather API) → YES, mock with WireMock
- Your own database → NO, use TestContainers
- Your own microservice → NO, use real service or TestContainers
- Message broker (Kafka, RabbitMQ) → NO, use TestContainers
- Cache (Redis) → NO, use TestContainers

**Q: How do I ensure parallel execution works?**
- Use UUID for all test data identifiers
- Implement targeted cleanup (never `deleteAll()`)
- No shared static state
- Use `RANDOM_PORT` for web tests

**Q: What should I test in integration tests?**
- Multi-layer workflows (controller → service → repository) → YES
- Database queries and persistence → YES
- REST API contracts → YES
- Security flows → YES
- Business logic edge cases → NO (unit tests)
- Validation edge cases → NO (unit tests)

**Q: How do I know if I should write an integration test or unit test?**
- Testing multiple layers working together → Integration test
- Testing with real database → Integration test
- Testing business logic in isolation → Unit test
- Test needs to be < 100ms → Unit test
- Test needs real infrastructure → Integration test

### Final Verification Before Generating Tests

**CRITICAL RULE**: Verify these items immediately before generating any integration test code.

**Examples**:

✅ Checklist:
- I have reviewed the Pre-Implementation Checklist
- I am using appropriate test slice (`@SpringBootTest`, `@DataMongoTest`, or `@WebMvcTest`)
- I am using TestContainers for all infrastructure (never embedded databases)
- I am using UUID for all test data to enable parallel execution
- I am implementing targeted cleanup in `@AfterEach` (never `deleteAll()`)
- I am using `@DynamicPropertySource` for TestContainers configuration
- I am using `RANDOM_PORT` for web tests
- I am mocking only external services (using WireMock)
- I am following `should<Behavior>When<Condition>` naming convention
- I am using Given-When-Then structure with comments
- I am not duplicating unit test coverage
- My tests focus on component integration, not business logic details

---

## Pre-Implementation Checklist

### Using This Checklist Effectively

**CRITICAL RULE**: This checklist consolidates ALL requirements from this document - review before writing any integration test.

**Examples**:

Checklist workflow:
1. **Before starting**: Review entire checklist to understand full scope
2. **During implementation**: Check off items as you complete them
3. **Before code review**: Verify all items are checked
4. **During review**: Confirm all items are actually implemented
5. **If item cannot be checked**: Review the relevant section above for guidance

### Required Verifications

### Test Framework Setup
- [ ] Using appropriate test slice (`@SpringBootTest`, `@DataMongoTest`, or `@WebMvcTest`)
- [ ] Using TestContainers for all infrastructure (MongoDB, Redis, Kafka)
- [ ] NOT using embedded or in-memory databases
- [ ] Test file ends with `IntegrationTest.java`
- [ ] Test file is in `src/test/java` with matching package structure
- [ ] Using `@ActiveProfiles("integration-test")` if needed

### Parallel Execution Design
- [ ] Using UUID for all test data identifiers
- [ ] No hardcoded values that could conflict across tests
- [ ] No shared static state between tests
- [ ] Using `RANDOM_PORT` for web tests
- [ ] Tests are completely independent with no execution order dependencies

### TestContainers Configuration
- [ ] Using `@Testcontainers` annotation
- [ ] Containers declared as `static @Container` fields
- [ ] Using `.withReuse(true)` for performance optimization
- [ ] Using `@DynamicPropertySource` for dynamic configuration
- [ ] Not hardcoding ports or connection strings

### Test Data Management
- [ ] Tracking test-specific entity IDs in lists (e.g., `testUserIds`)
- [ ] Implementing targeted cleanup in `@AfterEach`
- [ ] NOT using `deleteAll()` (only `deleteAllById()` with test IDs)
- [ ] Clearing tracking lists after cleanup
- [ ] Using unique identifiers to filter query results

### Test Structure
- [ ] Test methods follow `should<Behavior>When<Condition>` naming convention
- [ ] Each test uses Given-When-Then structure with comments
- [ ] Tests focus on multi-layer integration (not business logic details)
- [ ] Not duplicating unit test coverage
- [ ] Each test is focused on one integration scenario

### Mocking Strategy
- [ ] Only mocking external services outside your control
- [ ] Using WireMock for external HTTP service mocking
- [ ] NOT mocking internal databases (using TestContainers)
- [ ] NOT mocking internal message brokers (using TestContainers)
- [ ] NOT mocking internal cache systems (using TestContainers)

### Assertions and Verification
- [ ] Using Hamcrest matchers for assertions
- [ ] Using `verify(mock, timeout())` for async operations (not `Thread.sleep()`)
- [ ] Assertions test end-to-end integration behavior
- [ ] Verifying data persistence in database where applicable
- [ ] Testing complete workflows through multiple layers

### Performance
- [ ] Each test completes in < 5 seconds (target)
- [ ] No `Thread.sleep()` or arbitrary waits
- [ ] Using proper waiting mechanisms for async operations
- [ ] Container reuse enabled where appropriate
- [ ] Using narrowest test slice for scope

### REST API Testing
- [ ] Using MockMvc or RestAssured consistently
- [ ] Testing HTTP status codes
- [ ] Testing request/response JSON structure
- [ ] Testing error scenarios with appropriate status codes
- [ ] Testing security/authentication where applicable

### Best Practices
- [ ] Not testing framework functionality
- [ ] Not testing business logic edge cases (that's for unit tests)
- [ ] Cleanup implemented and verified
- [ ] Using test profiles when needed
- [ ] Test names are descriptive and clear
- [ ] No port or configuration hardcoding

---

## References

- **Spring Boot Testing Documentation**: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- **TestContainers Documentation**: https://www.testcontainers.org/
- **MockMvc Documentation**: https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html
- **RestAssured Documentation**: https://rest-assured.io/
- **WireMock Documentation**: https://wiremock.org/
