# AI-Assisted Testing Rules

This directory contains comprehensive, AI-ready testing rules for both frontend and backend development. These rules are designed to enable AI assistants (like Claude, GPT, etc.) to generate high-quality, consistent tests that follow project standards.

## 📁 Structure

```
doc/rules/
├── README.md (this file)
├── frontend-unit-testing.md          # React/TypeScript unit testing rules
├── frontend-integration-testing.md   # React/TypeScript integration testing rules
├── backend-unit-testing.md           # Java/SpringBoot unit testing rules
└── backend-integration-testing.md    # Java/SpringBoot integration testing rules
```

## 🎯 Purpose

These rules serve multiple purposes:

1. **AI Test Generation**: Provide detailed context for AI to write tests that match project standards
2. **Team Consistency**: Ensure all developers (human and AI) follow the same testing patterns
3. **Knowledge Base**: Document testing best practices and project-specific conventions
4. **Onboarding**: Help new team members understand project testing philosophy

## 📚 Frontend Rules

### [Frontend Unit Testing](./frontend-unit-testing.md)
- Component logic testing with Vitest
- Spy usage patterns for prop validation
- 100% coverage approach
- Data-testid conventions
- Mock strategies

### [Frontend Integration Testing](./frontend-integration-testing.md)
- Complete user workflow testing
- Real integration testing (minimal mocking)
- Form testing patterns (standalone & multistep)
- Third-party dependency mocking
- Test pollution prevention

## ☕ Backend Rules

### [Backend Unit Testing](./backend-unit-testing.md)
- JUnit5 + Mockito patterns
- Mock vs @SpringBootTest guidance
- Test naming conventions (should-when)
- Given-When-Then structure
- Edge case coverage

### [Backend Integration Testing](./backend-integration-testing.md)
- @SpringBootTest usage
- TestContainers for MongoDB/Kafka
- REST API testing with MockMvc
- HTTP status validation
- Test data cleanup

## 🤖 How AI Uses These Rules

### Cursor AI (.cursorrules)
The project's `.cursorrules` file references these rules, making them automatically available to Cursor AI when writing tests in this repository.

### Manual AI Usage
When using AI assistants (ChatGPT, Claude, etc.) outside of Cursor:

1. **Copy relevant rule file** (e.g., `frontend-unit-testing.md`)
2. **Paste into AI chat** with your code
3. **Request test generation** referencing the rules

Example prompt:
```
Using the rules from frontend-unit-testing.md (attached), 
please write unit tests for this CreateTaskModal component:
[paste component code]
```

## 📖 Related Documentation

- [Testing Standards](../testing-standards.md) - Overview of testing philosophy
- [Testing Guide](../testing-guide.md) - Comprehensive testing checklist
- [ADR-001: Vitest over Jest](../adr/001-vitest-over-jest.md) - Frontend testing framework decision
- [Check List](../check-list.md) - Quick testing pyramid checklist

## 🔄 Maintenance

These rules are living documents. Update them when:

- New testing patterns emerge
- Project standards evolve
- Technology stack changes
- Team feedback identifies gaps

## 💡 Philosophy

> "Tests should be so clear that they serve as executable documentation. AI-generated tests should be indistinguishable from human-written tests."

These rules embody this philosophy by:

- **Specificity**: Exact patterns, not vague guidelines
- **Examples**: Real code examples from the project
- **Rationale**: Why, not just what
- **Consistency**: Same conventions across all tests
- **Practicality**: Focus on what actually matters

---

**Note**: These rules complement, not replace, human judgment. Use them as guardrails, not prison bars.

