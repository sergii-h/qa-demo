---
type: testing-standards
technology: react, typescript, javascript
test-type: unit-testing
frameworks: vitest, jest, react-testing-library
audience: developers, ai-assistants
applies-to: frontend, react-applications
---

# Frontend Unit Testing Standards

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
- **While writing tests**: Reference specific sections (Component Testing, Hooks Testing, etc.)
- **During code review**: Check "Common Pitfalls to Avoid" → Verify against "AI Assistant Instructions"
- **For troubleshooting**: Use section titles to find relevant patterns and examples

---

## Quick Reference Patterns

### Standard Component Unit Test Setup

**CRITICAL RULE**: All component unit tests must use React Testing Library with Vitest/Jest and focus on testing logic, not implementation details.

**Examples**:

```typescript
// ✅ Correct: Component unit test with React Testing Library
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { UserProfile } from './UserProfile';

describe('UserProfile', () => {
  const mockOnSave = vi.fn();
  const defaultProps = {
    userId: 'user-123',
    onSave: mockOnSave,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render user profile with provided data', () => {
    // Given: Component with props
    render(<UserProfile {...defaultProps} name="John Doe" />);
    
    // When: Component renders
    const nameElement = screen.getByTestId('user-name');
    
    // Then: Displays correct data
    expect(nameElement).toHaveTextContent('John Doe');
  });

  it('should call onSave when save button is clicked', async () => {
    // Given: Component rendered
    const user = userEvent.setup();
    render(<UserProfile {...defaultProps} />);
    
    // When: User clicks save button
    const saveButton = screen.getByTestId('save-button');
    await user.click(saveButton);
    
    // Then: Callback is invoked
    expect(mockOnSave).toHaveBeenCalledTimes(1);
    expect(mockOnSave).toHaveBeenCalledWith({ userId: 'user-123' });
  });
});
```

### Essential Do's and Don'ts

**MANDATORY**: Follow these core practices for all frontend unit tests.

**Examples**:

✅ **DO**:
- Test component logic and behavior (not implementation details)
- Use `data-testid` for element selection (not text or CSS selectors)
- Cover all business logic and requirements specified in the ticket
- Achieve 90% code coverage for all components and utilities
- Use `waitFor()` for async operations (never `setTimeout`)
- Use spies (`vi.fn()`) to verify function calls and prop passing
- Create minimal mocks that return only what the test requires
- Test user interactions with `@testing-library/user-event`
- Use `beforeEach` to reset mocks and state between tests
- Focus on testing logic, not constants

❌ **DON'T**:
- Test implementation details (state variables, internal methods visibility)
- Use text content for element selection (use `data-testid`)
- Test `data-testid` prop passing to child components
- Use static waits like `setTimeout` (use `waitFor()` instead)
- Write meaningless assertions like `expect(true).toBe(true)`
- Test third-party library internals (e.g., Zod validation logic)
- Create complex mock implementations (keep mocks minimal and focused)
- Test constants without logic

---

## Key Principles

### Focus on Logic and Behavior

**RULE**: Unit tests must focus on testing component logic, user interactions, and behavior - not implementation details or styling.

**Examples**:

Test these aspects:
- **Component logic**: Conditional rendering, state changes, computed values
- **User interactions**: Clicks, form inputs, keyboard navigation
- **Prop handling**: Correct prop passing to child components (use spies)
- **Function calls**: Callbacks, event handlers, side effects
- **Edge cases**: Empty states, error scenarios, boundary conditions
- **Data transformations**: Formatting, filtering, sorting logic

Don't test these:
- Constants without logic
- Third-party library internals
- CSS classes or styling
- Implementation details (private state, method names)

### Comprehensive Coverage

**MANDATORY**: Cover all business logic and functional requirements from the ticket description that are testable at unit level (focusing on logic that can be tested in isolation).

**Examples**:
- Map each requirement to specific test cases
- Cover all code paths and edge cases
- Test boundary conditions (empty values, null inputs, min/max values)
- Test error handling and exception scenarios

### Achieve 90% Code Coverage

**CRITICAL RULE**: All frontend code must have at least 90% unit test coverage - aim to test all critical lines, branches, and functions.

**Examples**:

Coverage requirements:
- **Statements**: 90%
- **Branches**: 90%
- **Functions**: 90%
- **Lines**: 90%

Build configuration:
```javascript
// vitest.config.ts
export default defineConfig({
  test: {
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      statements: 90,
      branches: 90,
      functions: 90,
      lines: 90,
      exclude: [
        '**/*.stories.tsx',
        '**/*.spec.tsx',
        '**/index.ts',
        '**/*.d.ts',
      ],
    },
  },
});
```

What to exclude from coverage:
- Storybook stories (`*.stories.tsx`)
- Test files themselves
- Type definition files (`*.d.ts`)
- Barrel exports (`index.ts` with only exports)

### Use Spies for Verification

**MANDATORY**: Use spies (mocked functions) to verify prop passing, function calls, and component interactions without full rendering.

**Examples**:

```typescript
// ✅ Good: Using spy to verify callback invocation
it('should call onSubmit with form data', async () => {
  const onSubmitSpy = vi.fn();
  const user = userEvent.setup();
  
  render(<ContactForm onSubmit={onSubmitSpy} />);
  
  await user.type(screen.getByTestId('email-input'), 'test@example.com');
  await user.click(screen.getByTestId('submit-button'));
  
  expect(onSubmitSpy).toHaveBeenCalledWith({
    email: 'test@example.com',
  });
});

// ✅ Good: Using spy to verify prop passing to child
it('should pass correct props to UserCard component', () => {
  const UserCardSpy = vi.fn(() => null);
  vi.mock('./UserCard', () => ({ UserCard: UserCardSpy }));
  
  render(<UserList users={mockUsers} />);
  
  expect(UserCardSpy).toHaveBeenCalledWith(
    expect.objectContaining({
      userId: 'user-1',
      name: 'John Doe',
    }),
    expect.anything()
  );
});
```

### Test Real User Interactions

**RULE**: Test components as users would interact with them using `@testing-library/user-event`.

**Examples**:

```typescript
import userEvent from '@testing-library/user-event';

it('should handle complete user registration flow', async () => {
  const user = userEvent.setup();
  const onRegisterSpy = vi.fn();
  
  render(<RegistrationForm onRegister={onRegisterSpy} />);
  
  // User types in fields
  await user.type(screen.getByTestId('email-input'), 'john@example.com');
  await user.type(screen.getByTestId('password-input'), 'SecurePass123!');
  
  // User checks terms checkbox
  await user.click(screen.getByTestId('terms-checkbox'));
  
  // User submits form
  await user.click(screen.getByTestId('register-button'));
  
  await waitFor(() => {
    expect(onRegisterSpy).toHaveBeenCalledWith({
      email: 'john@example.com',
      password: 'SecurePass123!',
      acceptedTerms: true,
    });
  });
});
```

---

## Framework and Tools

### Testing Framework Stack

**MANDATORY**: Use these frameworks for frontend unit testing.

**Examples**:

Required frameworks:
- **Vitest or Jest**: Testing framework and test runner
- **React Testing Library**: Component rendering and interaction testing
- **@testing-library/user-event**: Realistic user interaction simulation
- **@testing-library/jest-dom**: Extended DOM matchers
- **Vitest UI (optional)**: Visual test running interface

### File Naming and Location

**MANDATORY**: Follow consistent naming conventions for test files.

**Examples**:

Test file naming:
- Unit test files must end with `.unit.spec.tsx` or `.spec.tsx`
- Pattern: `<ComponentName>.unit.spec.tsx`
- Examples: `UserProfile.unit.spec.tsx`, `LoginForm.spec.tsx`, `useAuth.spec.ts`

Location:
- Place unit tests next to the component/file being tested
- Alternative: Mirror structure in `__tests__` directory
- Keep test files co-located with source files for easy navigation

Test method naming:
- Use descriptive names: `should <expected behavior> when <condition>`
- Examples:
  - `should render error message when validation fails`
  - `should call onSubmit when form is valid`
  - `should disable submit button while loading`

---

## Component Testing

### Component Testing Patterns

**RULE**: Test components by rendering them and verifying behavior through user interactions and output.

**Examples**:

Component testing scope:
- **Rendering**: Component displays correctly with different props
- **Conditional logic**: Shows/hides elements based on props or state
- **User interactions**: Responds correctly to clicks, inputs, etc.
- **Prop passing**: Passes correct props to child components
- **Callbacks**: Invokes callback props with correct arguments
- **Edge cases**: Handles loading, error, and empty states

### Testing Component Rendering

**RULE**: Verify components render correctly with different prop combinations.

**Examples**:

```typescript
describe('ProductCard', () => {
  it('should render product with all details', () => {
    // Given: Product with full details
    const product = {
      id: 'prod-1',
      name: 'Laptop',
      price: 999.99,
      inStock: true,
      imageUrl: '/laptop.jpg',
    };
    
    // When: Component renders
    render(<ProductCard product={product} />);
    
    // Then: All details are displayed
    expect(screen.getByTestId('product-name')).toHaveTextContent('Laptop');
    expect(screen.getByTestId('product-price')).toHaveTextContent('$999.99');
    expect(screen.getByTestId('stock-badge')).toHaveTextContent('In Stock');
  });

  it('should render out of stock badge when product unavailable', () => {
    // Given: Out of stock product
    const product = { id: 'prod-1', name: 'Laptop', price: 999.99, inStock: false };
    
    // When: Component renders
    render(<ProductCard product={product} />);
    
    // Then: Out of stock badge shown
    expect(screen.getByTestId('stock-badge')).toHaveTextContent('Out of Stock');
    expect(screen.getByTestId('add-to-cart-button')).toBeDisabled();
  });
});
```

### Testing User Interactions

**MANDATORY**: Test user interactions using `@testing-library/user-event` for realistic behavior.

**Examples**:

```typescript
import userEvent from '@testing-library/user-event';

describe('SearchBar', () => {
  it('should call onSearch when user types and submits', async () => {
    // Given: Component with search callback
    const user = userEvent.setup();
    const onSearchSpy = vi.fn();
    render(<SearchBar onSearch={onSearchSpy} />);
    
    // When: User types query and presses Enter
    const input = screen.getByTestId('search-input');
    await user.type(input, 'laptops{Enter}');
    
    // Then: Search callback invoked with query
    expect(onSearchSpy).toHaveBeenCalledWith('laptops');
  });

  it('should clear input when clear button clicked', async () => {
    // Given: Component with text in input
    const user = userEvent.setup();
    render(<SearchBar />);
    const input = screen.getByTestId('search-input') as HTMLInputElement;
    await user.type(input, 'test query');
    
    // When: User clicks clear button
    await user.click(screen.getByTestId('clear-button'));
    
    // Then: Input is cleared
    expect(input.value).toBe('');
  });
});
```

### Testing Conditional Rendering

**RULE**: Test all critical conditional rendering paths to achieve 90% coverage.

**Examples**:

```typescript
describe('UserMenu', () => {
  it('should show login button when user not authenticated', () => {
    // Given: No user authenticated
    render(<UserMenu isAuthenticated={false} />);
    
    // Then: Login button visible, profile hidden
    expect(screen.getByTestId('login-button')).toBeInTheDocument();
    expect(screen.queryByTestId('profile-menu')).not.toBeInTheDocument();
  });

  it('should show profile menu when user authenticated', () => {
    // Given: User authenticated
    render(<UserMenu isAuthenticated={true} user={{ name: 'John' }} />);
    
    // Then: Profile menu visible, login button hidden
    expect(screen.getByTestId('profile-menu')).toBeInTheDocument();
    expect(screen.queryByTestId('login-button')).not.toBeInTheDocument();
  });

  it('should show loading state while fetching user data', () => {
    // Given: Loading state
    render(<UserMenu isLoading={true} />);
    
    // Then: Loading indicator shown
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
  });
});
```

### Testing Async Operations

**CRITICAL RULE**: Use `waitFor()` for async operations - never use `setTimeout` or static waits.

**Examples**:

```typescript
describe('UserProfile', () => {
  it('should load and display user data', async () => {
    // Given: Mock API returns user data
    const mockFetchUser = vi.fn().mockResolvedValue({
      id: 'user-1',
      name: 'John Doe',
      email: 'john@example.com',
    });
    
    // When: Component renders and fetches data
    render(<UserProfile userId="user-1" fetchUser={mockFetchUser} />);
    
    // Then: Loading state shown initially
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    
    // And: User data displayed after load
    await waitFor(() => {
      expect(screen.getByTestId('user-name')).toHaveTextContent('John Doe');
    });
    expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument();
  });

  it('should display error message when fetch fails', async () => {
    // Given: Mock API rejects with error
    const mockFetchUser = vi.fn().mockRejectedValue(new Error('Failed to load'));
    
    // When: Component renders and fetch fails
    render(<UserProfile userId="user-1" fetchUser={mockFetchUser} />);
    
    // Then: Error message displayed
    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('Failed to load');
    });
  });
});
```

---

## Testing Custom Hooks

### Hook Testing Patterns

**RULE**: Test custom hooks using `renderHook` from React Testing Library.

**Examples**:

```typescript
import { renderHook, waitFor } from '@testing-library/react';
import { act } from 'react-dom/test-utils';
import { useCounter } from './useCounter';

describe('useCounter', () => {
  it('should initialize with default value', () => {
    // Given & When: Hook renders with default
    const { result } = renderHook(() => useCounter());
    
    // Then: Initial value is 0
    expect(result.current.count).toBe(0);
  });

  it('should initialize with provided value', () => {
    // Given & When: Hook renders with initial value
    const { result } = renderHook(() => useCounter(10));
    
    // Then: Initial value is 10
    expect(result.current.count).toBe(10);
  });

  it('should increment count when increment called', () => {
    // Given: Hook rendered
    const { result } = renderHook(() => useCounter(0));
    
    // When: Increment called
    act(() => {
      result.current.increment();
    });
    
    // Then: Count increased
    expect(result.current.count).toBe(1);
  });

  it('should reset count to initial value', () => {
    // Given: Hook with modified count
    const { result } = renderHook(() => useCounter(5));
    act(() => {
      result.current.increment();
      result.current.increment();
    });
    
    // When: Reset called
    act(() => {
      result.current.reset();
    });
    
    // Then: Count reset to initial value
    expect(result.current.count).toBe(5);
  });
});
```

### Testing Async Hooks

**RULE**: Use `waitFor()` when testing hooks with async operations.

**Examples**:

```typescript
describe('useFetchUser', () => {
  it('should fetch and return user data', async () => {
    // Given: Mock API
    const mockUser = { id: '1', name: 'John' };
    const mockFetch = vi.fn().mockResolvedValue(mockUser);
    
    // When: Hook fetches user
    const { result } = renderHook(() => useFetchUser('1', mockFetch));
    
    // Then: Initial loading state
    expect(result.current.loading).toBe(true);
    expect(result.current.user).toBeNull();
    
    // And: User loaded after async operation
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.user).toEqual(mockUser);
    });
  });

  it('should handle fetch errors', async () => {
    // Given: Mock API throws error
    const mockFetch = vi.fn().mockRejectedValue(new Error('Network error'));
    
    // When: Hook attempts to fetch
    const { result } = renderHook(() => useFetchUser('1', mockFetch));
    
    // Then: Error state set
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBe('Network error');
    });
  });
});
```

---

## Testing Utilities and Pure Functions

### Utility Function Testing

**RULE**: Test utility functions and pure functions thoroughly with all edge cases.

**Examples**:

```typescript
import { formatCurrency, validateEmail, debounce } from './utils';

describe('formatCurrency', () => {
  it('should format number with currency symbol', () => {
    expect(formatCurrency(1234.56, 'USD')).toBe('$1,234.56');
  });

  it('should handle zero value', () => {
    expect(formatCurrency(0, 'USD')).toBe('$0.00');
  });

  it('should handle negative values', () => {
    expect(formatCurrency(-500, 'USD')).toBe('-$500.00');
  });

  it('should round to two decimal places', () => {
    expect(formatCurrency(10.999, 'USD')).toBe('$11.00');
  });
});

describe('validateEmail', () => {
  it('should return true for valid email', () => {
    expect(validateEmail('user@example.com')).toBe(true);
  });

  it('should return false for invalid email formats', () => {
    expect(validateEmail('invalid')).toBe(false);
    expect(validateEmail('user@')).toBe(false);
    expect(validateEmail('@example.com')).toBe(false);
    expect(validateEmail('')).toBe(false);
  });
});

describe('debounce', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should delay function execution', () => {
    const fn = vi.fn();
    const debouncedFn = debounce(fn, 300);
    
    debouncedFn();
    expect(fn).not.toHaveBeenCalled();
    
    vi.advanceTimersByTime(300);
    expect(fn).toHaveBeenCalledTimes(1);
  });

  it('should only execute once for multiple rapid calls', () => {
    const fn = vi.fn();
    const debouncedFn = debounce(fn, 300);
    
    debouncedFn();
    debouncedFn();
    debouncedFn();
    
    vi.advanceTimersByTime(300);
    expect(fn).toHaveBeenCalledTimes(1);
  });
});
```

---

## Mocking and Spies

### What to Mock

**MANDATORY**: Mock external dependencies, third-party libraries, and child components to achieve true unit test isolation. Keep internal utility functions real and test them directly.

**Examples**:

Mock these:
- **External APIs**: HTTP requests, fetch calls
- **Third-party libraries**: Analytics, payment processors, external SDKs
- **Child components**: Mock to achieve true unit test isolation and faster tests
- **Browser APIs**: localStorage, sessionStorage, navigator, window.location
- **Date/Time**: `Date.now()`, timers for consistent tests
- **Random functions**: `Math.random()`, `uuid()` for predictable tests
- **Environment variables**: Process.env values

Don't mock:
- Internal utility functions (test them directly)
- React itself or React Testing Library
- Simple data transformations
- Constants and configuration

### Creating Minimal Mocks

**CRITICAL RULE**: Create minimal mocks that return only what the test requires - avoid complex mock implementations.

**Examples**:

```typescript
// ✅ Good: Minimal mock returning only needed data
it('should display user profile', () => {
  const mockFetchUser = vi.fn().mockResolvedValue({
    id: 'user-1',
    name: 'John Doe',
  });
  
  render(<UserProfile fetchUser={mockFetchUser} />);
  // Test continues...
});

// ❌ Bad: Complex mock with unnecessary logic
it('should display user profile', () => {
  const mockFetchUser = vi.fn().mockImplementation(async (userId) => {
    if (userId === 'admin') {
      return { id: userId, name: 'Admin', role: 'admin', permissions: [...] };
    } else if (userId.startsWith('user')) {
      const userNumber = userId.split('-')[1];
      return {
        id: userId,
        name: `User ${userNumber}`,
        createdAt: new Date().toISOString(),
        // ... lots of complex logic
      };
    }
    throw new Error('Invalid user ID');
  });
  // Overly complex!
});
```

### Mocking Child Components

**RULE**: Mock child components to achieve true unit test isolation - test only the component's logic without dependencies on child implementations.

**Examples**:

```typescript
// ✅ Good: Mocking child component for isolation
import { render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import { UserProfile } from './UserProfile';

// Mock the child components
vi.mock('./UserCard', () => ({
  UserCard: vi.fn(({ user }) => (
    <div data-testid="mock-user-card">{user.name}</div>
  )),
}));

vi.mock('./UserStats', () => ({
  UserStats: vi.fn(() => <div data-testid="mock-user-stats">Stats</div>),
}));

describe('UserProfile', () => {
  it('should render with user data and pass correct props to UserCard', () => {
    // Given: User profile data
    const user = { id: '1', name: 'John Doe', email: 'john@example.com' };
    
    // When: Component renders
    render(<UserProfile user={user} />);
    
    // Then: Child components rendered
    expect(screen.getByTestId('mock-user-card')).toHaveTextContent('John Doe');
    expect(screen.getByTestId('mock-user-stats')).toBeInTheDocument();
  });
});

// ✅ Good: Using spy to verify props passed to mocked child
import { UserCard } from './UserCard';

vi.mock('./UserCard', () => ({
  UserCard: vi.fn(() => <div data-testid="mock-user-card" />),
}));

it('should pass correct props to UserCard component', () => {
  const mockUser = { id: 'user-1', name: 'John Doe', role: 'admin' };
  
  render(<UserList users={[mockUser]} />);
  
  // Verify UserCard was called with correct props
  expect(UserCard).toHaveBeenCalledWith(
    expect.objectContaining({
      user: mockUser,
      isEditable: true,
    }),
    expect.anything() // React context
  );
});

// ❌ Bad: Not mocking child components (slower, less isolated)
describe('UserProfile', () => {
  it('should render user profile', () => {
    // This renders the entire component tree including all children
    // - Slower test execution
    // - Less isolation (child component bugs affect this test)
    // - Testing child logic multiple times across different parent tests
    render(<UserProfile user={mockUser} />);
    
    // Now testing both parent AND child implementation details
    expect(screen.getByTestId('user-card-avatar')).toBeInTheDocument();
    expect(screen.getByTestId('user-stats-counter')).toBeInTheDocument();
  });
});
```

### Using Spies to Verify Function Calls

**RULE**: Use spies to verify that functions are called with correct arguments.

**Examples**:

```typescript
describe('OrderConfirmation', () => {
  it('should call onConfirm with order details', async () => {
    // Given: Component with spy callback
    const user = userEvent.setup();
    const onConfirmSpy = vi.fn();
    const order = { id: 'order-1', total: 99.99 };
    
    render(<OrderConfirmation order={order} onConfirm={onConfirmSpy} />);
    
    // When: User confirms order
    await user.click(screen.getByTestId('confirm-button'));
    
    // Then: Spy called with correct arguments
    expect(onConfirmSpy).toHaveBeenCalledTimes(1);
    expect(onConfirmSpy).toHaveBeenCalledWith({
      orderId: 'order-1',
      confirmedAt: expect.any(String),
    });
  });

  it('should call analytics tracking on confirmation', async () => {
    // Given: Mock analytics
    const user = userEvent.setup();
    const trackEventSpy = vi.fn();
    vi.mock('./analytics', () => ({ trackEvent: trackEventSpy }));
    
    render(<OrderConfirmation order={mockOrder} />);
    
    // When: User confirms
    await user.click(screen.getByTestId('confirm-button'));
    
    // Then: Analytics tracked
    expect(trackEventSpy).toHaveBeenCalledWith('order_confirmed', {
      orderId: 'order-1',
      value: 99.99,
    });
  });
});
```

### Mocking Third-Party Libraries

**RULE**: Mock third-party library functions and verify they're called correctly - don't test library internals.

**Examples**:

```typescript
// ✅ Good: Mock Zod and verify it's called correctly
import { z } from 'zod';

vi.mock('zod', () => ({
  z: {
    string: vi.fn().mockReturnValue({
      email: vi.fn().mockReturnThis(),
      min: vi.fn().mockReturnThis(),
    }),
    object: vi.fn().mockReturnValue({
      parse: vi.fn(),
    }),
  },
}));

describe('RegistrationForm', () => {
  it('should validate email using Zod', () => {
    render(<RegistrationForm />);
    
    // Verify Zod methods called
    expect(z.string).toHaveBeenCalled();
    expect(z.string().email).toHaveBeenCalled();
  });
});

// ❌ Bad: Testing Zod's validation logic
it('should reject invalid email format', () => {
  // Don't test Zod's internal validation!
  const schema = z.string().email();
  expect(() => schema.parse('invalid')).toThrow();
});
```

---

## Testing Best Practices

### Use data-testid for Element Selection

**CRITICAL RULE**: Always use `data-testid` for selecting elements in tests - never use text content or CSS selectors.

**Examples**:

```typescript
// ✅ Good: Using data-testid
it('should display username', () => {
  render(<UserCard user={mockUser} />);
  expect(screen.getByTestId('username')).toHaveTextContent('John Doe');
});

// ❌ Bad: Using text content
it('should display username', () => {
  render(<UserCard user={mockUser} />);
  expect(screen.getByText('John Doe')).toBeInTheDocument(); // Fragile!
});

// ❌ Bad: Using CSS selectors
it('should display username', () => {
  const { container } = render(<UserCard user={mockUser} />);
  expect(container.querySelector('.username')).toHaveTextContent('John Doe'); // Fragile!
});
```

Component implementation with data-testid:
```typescript
export const UserCard = ({ user }: Props) => {
  return (
    <div data-testid="user-card">
      <h3 data-testid="username">{user.name}</h3>
      <p data-testid="user-email">{user.email}</p>
      <button data-testid="edit-button">Edit</button>
    </div>
  );
};
```

### Validate Data-Track Attributes

**RULE**: When components have analytics tracking attributes, validate they exist and have correct values.

**Examples**:

```typescript
it('should have correct tracking attributes for analytics', () => {
  render(<ProductCard product={mockProduct} />);
  
  const addToCartButton = screen.getByTestId('add-to-cart-button');
  expect(addToCartButton).toHaveAttribute('data-track', 'add_to_cart');
  expect(addToCartButton).toHaveAttribute('data-track-product-id', 'prod-123');
});
```

### Use beforeEach and afterEach Properly

**MANDATORY**: Use `beforeEach` to set up clean test state and `afterEach` to clean up mocks and side effects.

**Examples**:

```typescript
describe('ShoppingCart', () => {
  const mockLocalStorage: { [key: string]: string } = {};
  
  beforeEach(() => {
    // Clear all mocks before each test
    vi.clearAllMocks();
    
    // Setup localStorage mock
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: (key: string) => mockLocalStorage[key] || null,
        setItem: (key: string, value: string) => {
          mockLocalStorage[key] = value;
        },
        clear: () => {
          Object.keys(mockLocalStorage).forEach(key => delete mockLocalStorage[key]);
        },
      },
      writable: true,
    });
  });

  afterEach(() => {
    // Clean up localStorage
    window.localStorage.clear();
    
    // Restore all mocks
    vi.restoreAllMocks();
  });

  it('should save cart to localStorage', () => {
    const { result } = renderHook(() => useShoppingCart());
    
    act(() => {
      result.current.addItem({ id: 'item-1', name: 'Product' });
    });
    
    expect(window.localStorage.setItem).toHaveBeenCalledWith(
      'cart',
      expect.stringContaining('item-1')
    );
  });
});
```

### Test Through Public APIs, Not Private Methods

**CRITICAL RULE**: Never test private methods directly - always test through the public API. Private methods should get coverage through public method tests.

**Examples**:

```typescript
// ❌ Bad: Testing private method using bracket notation
describe('FormValidator', () => {
  it('should validate email format using private method', () => {
    const validator = new FormValidator();
    
    // DON'T: Testing implementation details
    expect(validator['isValidEmail']('test@example.com')).toBe(true);
    expect(validator['isValidEmail']('invalid')).toBe(false);
  });
});

// ✅ Good: Test private logic through public API
describe('FormValidator', () => {
  it('should validate form with valid email', () => {
    // Given: Validator instance
    const validator = new FormValidator();
    
    // When: Validate form with valid email
    const result = validator.validate({ email: 'test@example.com' });
    
    // Then: Validation passes (private isValidEmail called internally)
    expect(result.isValid).toBe(true);
    expect(result.errors).toHaveLength(0);
  });

  it('should reject form with invalid email', () => {
    // Given: Validator instance
    const validator = new FormValidator();
    
    // When: Validate form with invalid email
    const result = validator.validate({ email: 'invalid' });
    
    // Then: Validation fails (private isValidEmail called internally)
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain('Invalid email format');
  });
});

// ✅ Good: Extract complex private logic to utility function
// If private method is complex enough to need direct testing, extract it

// utils/validation.ts
export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// validation.spec.ts
describe('isValidEmail', () => {
  it('should return true for valid email', () => {
    expect(isValidEmail('test@example.com')).toBe(true);
    expect(isValidEmail('user+tag@domain.co.uk')).toBe(true);
  });

  it('should return false for invalid email', () => {
    expect(isValidEmail('invalid')).toBe(false);
    expect(isValidEmail('@domain.com')).toBe(false);
    expect(isValidEmail('user@')).toBe(false);
  });
});

// FormValidator.ts - now uses the utility
import { isValidEmail } from './utils/validation';

export class FormValidator {
  validate(data: FormData) {
    const errors: string[] = [];
    
    if (!isValidEmail(data.email)) {
      errors.push('Invalid email format');
    }
    
    return { isValid: errors.length === 0, errors };
  }
}
```

**When you find yourself wanting to test private methods**:
1. First, try to reach the code through public API tests
2. If that's not possible, consider extracting to a utility function
3. If it's still private, question whether the code is actually needed
4. Never use bracket notation to access private members in tests

---

## Common Pitfalls to Avoid

### Understanding Anti-Patterns

**CRITICAL RULE**: Avoid these common mistakes that reduce test quality and maintainability.

**Examples**:

Review these pitfalls regularly:
1. Testing implementation details instead of behavior
2. Using text content for element selection
3. Static waits with `setTimeout`
4. Complex mock implementations
5. Meaningless assertions
6. Not achieving 90% coverage
7. Testing third-party library internals
8. Not cleaning up mocks between tests

---

### 1. Testing Implementation Details

❌ **DON'T**: Test internal state, private variables, or implementation details.

```typescript
// ❌ Bad: Testing internal state
it('should update internal counter state', () => {
  const { result } = renderHook(() => useCounter());
  act(() => result.current.increment());
  
  // Testing implementation detail
  expect(result.current['internalCounter']).toBe(1);
});

// ❌ Bad: Testing that component uses useState
it('should use useState for count', () => {
  const useStateSpy = vi.spyOn(React, 'useState');
  render(<Counter />);
  expect(useStateSpy).toHaveBeenCalled();
});
```

✅ **DO**: Test observable behavior and outputs.

```typescript
// ✅ Good: Testing behavior through output
it('should display incremented count', async () => {
  const user = userEvent.setup();
  render(<Counter />);
  
  await user.click(screen.getByTestId('increment-button'));
  
  expect(screen.getByTestId('count-display')).toHaveTextContent('1');
});
```

### 2. Using Text Content for Selection

❌ **DON'T**: Select elements by text content or CSS classes - it's fragile.

```typescript
// ❌ Bad: Selecting by text
it('should show error message', () => {
  render(<LoginForm />);
  expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
});

// ❌ Bad: Using CSS selectors
it('should show error message', () => {
  const { container } = render(<LoginForm />);
  expect(container.querySelector('.error-message')).toBeInTheDocument();
});
```

✅ **DO**: Always use `data-testid` for reliable element selection.

```typescript
// ✅ Good: Using data-testid
it('should show error message', () => {
  render(<LoginForm hasError={true} />);
  expect(screen.getByTestId('error-message')).toBeInTheDocument();
  expect(screen.getByTestId('error-message')).toHaveTextContent('Invalid credentials');
});
```

### 3. Static Waits with setTimeout

❌ **DON'T**: Use `setTimeout` or arbitrary waits - tests become slow and flaky.

```typescript
// ❌ Bad: Using setTimeout
it('should load user data', async () => {
  render(<UserProfile userId="1" />);
  
  await new Promise(resolve => setTimeout(resolve, 1000)); // Slow and flaky!
  
  expect(screen.getByTestId('username')).toHaveTextContent('John');
});
```

✅ **DO**: Use `waitFor()` to wait for actual conditions.

```typescript
// ✅ Good: Using waitFor
it('should load user data', async () => {
  render(<UserProfile userId="1" />);
  
  await waitFor(() => {
    expect(screen.getByTestId('username')).toHaveTextContent('John');
  });
});

// ✅ Good: Using findBy queries (built-in waiting)
it('should load user data', async () => {
  render(<UserProfile userId="1" />);
  
  const username = await screen.findByTestId('username');
  expect(username).toHaveTextContent('John');
});
```

### 4. Complex Mock Implementations

❌ **DON'T**: Create complex mock implementations with lots of logic.

```typescript
// ❌ Bad: Complex mock with conditional logic
const mockFetchUser = vi.fn().mockImplementation(async (userId, options) => {
  if (!userId) throw new Error('User ID required');
  if (userId === 'admin') {
    return {
      id: userId,
      name: 'Admin User',
      role: 'admin',
      permissions: ['read', 'write', 'delete'],
      settings: { theme: 'dark', notifications: true },
    };
  }
  if (userId.startsWith('user-')) {
    const num = parseInt(userId.split('-')[1]);
    return {
      id: userId,
      name: `User ${num}`,
      role: num > 100 ? 'premium' : 'basic',
      createdAt: new Date(2020, 0, num).toISOString(),
    };
  }
  throw new Error('User not found');
});
```

✅ **DO**: Create minimal mocks that return only what the test needs.

```typescript
// ✅ Good: Minimal mock per test
it('should display admin user', async () => {
  const mockFetchUser = vi.fn().mockResolvedValue({
    id: 'admin',
    name: 'Admin User',
    role: 'admin',
  });
  
  render(<UserProfile userId="admin" fetchUser={mockFetchUser} />);
  
  await waitFor(() => {
    expect(screen.getByTestId('user-role')).toHaveTextContent('admin');
  });
});
```

### 5. Meaningless Assertions

❌ **DON'T**: Write assertions that don't test actual logic.

```typescript
// ❌ Bad: Testing nothing
it('should pass', () => {
  expect(true).toBe(true);
});

// ❌ Bad: Testing constants
it('should have correct button text', () => {
  expect(BUTTON_TEXT).toBe('Submit');
});

// ❌ Bad: Testing obvious behavior
it('should create empty array', () => {
  expect([]).toEqual([]);
});
```

✅ **DO**: Write assertions that test actual component logic and behavior.

```typescript
// ✅ Good: Testing actual logic
it('should calculate total price correctly', () => {
  const items = [
    { id: '1', price: 10.00, quantity: 2 },
    { id: '2', price: 15.00, quantity: 1 },
  ];
  
  render(<Cart items={items} />);
  
  expect(screen.getByTestId('total-price')).toHaveTextContent('$35.00');
});
```

### 6. Not Achieving 90% Coverage

❌ **DON'T**: Leave critical code paths untested - aim for at least 90% coverage.

```typescript
// ❌ Bad: Only testing happy path
describe('UserForm', () => {
  it('should submit form with valid data', async () => {
    // Only tests valid submission
  });
  // Missing: error handling, validation, edge cases
});
```

✅ **DO**: Test all code paths, branches, and edge cases.

```typescript
// ✅ Good: Comprehensive coverage
describe('UserForm', () => {
  it('should submit form with valid data', async () => {
    // Happy path
  });

  it('should show validation error for invalid email', async () => {
    // Validation branch
  });

  it('should disable submit button while submitting', async () => {
    // Loading state
  });

  it('should display error message when submission fails', async () => {
    // Error handling
  });

  it('should reset form after successful submission', async () => {
    // Cleanup logic
  });
});
```

### 7. Testing Third-Party Library Internals

❌ **DON'T**: Test that third-party libraries work - trust they do.

```typescript
// ❌ Bad: Testing Zod validation logic
it('should validate email with Zod', () => {
  const schema = z.string().email();
  expect(() => schema.parse('invalid')).toThrow();
  expect(schema.parse('valid@email.com')).toBe('valid@email.com');
});

// ❌ Bad: Testing React Router behavior
it('should navigate when Link clicked', () => {
  // Testing react-router-dom internals
});
```

✅ **DO**: Mock libraries and verify your code calls them correctly.

```typescript
// ✅ Good: Verify Zod is called with correct config
vi.mock('zod', () => ({
  z: {
    string: vi.fn().mockReturnValue({
      email: vi.fn().mockReturnThis(),
      min: vi.fn().mockReturnThis(),
    }),
  },
}));

it('should configure email validation with Zod', () => {
  render(<RegistrationForm />);
  
  expect(z.string).toHaveBeenCalled();
  expect(z.string().email).toHaveBeenCalled();
  expect(z.string().min).toHaveBeenCalledWith(8);
});
```

### 8. Not Cleaning Up Mocks

❌ **DON'T**: Let mocks persist between tests - causes unexpected failures.

```typescript
// ❌ Bad: No cleanup
describe('UserService', () => {
  it('should fetch user', async () => {
    vi.fn().mockResolvedValue(mockUser);
    // No cleanup!
  });

  it('should handle error', async () => {
    // Previous mock still active!
  });
});
```

✅ **DO**: Clear mocks in `beforeEach` or `afterEach`.

```typescript
// ✅ Good: Proper cleanup
describe('UserService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should fetch user', async () => {
    const mockFetch = vi.fn().mockResolvedValue(mockUser);
    // Test...
  });

  it('should handle error', async () => {
    const mockFetch = vi.fn().mockRejectedValue(new Error());
    // Test...
  });
});
```

---

## AI Assistant Instructions

### Before Generating Any Test Code

**CRITICAL RULE**: AI assistants must complete this preparation before generating any frontend unit test code.

**Examples**:

1. **Read the Pre-Implementation Checklist** at the end of this document
2. **Review "Common Pitfalls to Avoid"** section to avoid anti-patterns
3. **Use "Quick Reference Patterns"** section for standard templates
4. **Verify 90% coverage strategy** - ensure critical branches and paths are tested
5. **Check data-testid usage** - ensure all elements have proper test IDs

### Mandatory Practices for AI-Generated Tests

**CRITICAL RULE**: Every frontend test generated by AI assistants must follow these practices without exception.

**Examples**:

1. **Always use data-testid**: Select elements using `data-testid`, never text content or CSS selectors.

2. **Map requirements to test cases**: Ensure all business logic and functional requirements from the ticket that are testable in isolation are covered by specific test cases.

3. **Achieve 90% coverage**: Test critical lines, branches, and functions. Aim for at least 90% coverage of all code paths.

4. **Use waitFor() for async**: Never use `setTimeout` or static waits. Always use `waitFor()` or `findBy` queries.

5. **Follow naming conventions**: Use descriptive test names: `should <behavior> when <condition>`.

6. **Use user-event for interactions**: Test user interactions with `@testing-library/user-event`, not `fireEvent`.

7. **Create minimal mocks**: Keep mocks simple and focused - only return what the test needs.

8. **Clean up between tests**: Use `beforeEach` to clear mocks and reset state.

9. **Test behavior, not implementation**: Focus on what users see and experience, not internal state.

10. **Don't test third-party libraries**: Mock them and verify your code calls them correctly.

11. **Use Given-When-Then**: Structure tests with clear Given-When-Then comments.

12. **Validate data-track attributes**: If components have analytics tracking, verify the attributes.

13. **Test all conditional paths**: Ensure 90% branch coverage by testing critical if/else/switch cases.

14. **Use spies for callbacks**: Verify callbacks and prop passing using `vi.fn()` spies.

### Quick Decision Trees for AI Assistants

**RULE**: Use these decision trees for quick answers when generating frontend tests.

**Examples**:

**Q: How should I select elements in tests?**
- Always use `data-testid` with `screen.getByTestId()`
- Never use text content, CSS selectors, or class names

**Q: Should I test this?**
- Component logic and behavior → YES
- User interactions → YES
- Conditional rendering → YES
- Prop passing to children (with spies) → YES
- Constants without logic → NO
- Third-party library internals → NO
- CSS styling or classes → NO

**Q: How should I handle async operations?**
- Use `waitFor()` from React Testing Library
- Use `findBy` queries (have built-in waiting)
- Never use `setTimeout` or `sleep`

**Q: What should I mock?**
- External APIs and fetch calls → YES, mock
- Third-party libraries → YES, mock and verify calls
- Child components → YES, mock for isolation and speed
- Browser APIs (localStorage, etc.) → YES, mock
- Your own utility functions → NO, test directly
- React or React Testing Library → NO, never mock

**Q: How do I achieve 90% coverage?**
- Test all critical conditional branches (if/else/switch)
- Test all user interaction paths
- Test loading, error, and success states
- Test edge cases (empty, null, error)
- Check coverage report and add tests for missed lines

**Q: Should I use fireEvent or userEvent?**
- Always use `userEvent` from `@testing-library/user-event`
- `userEvent` simulates real user interactions more accurately
- Only use `fireEvent` for events that `userEvent` doesn't support

### Final Verification Before Generating Tests

**CRITICAL RULE**: Verify these items immediately before generating any frontend test code.

**Examples**:

✅ Checklist:
- I have reviewed the Pre-Implementation Checklist
- I have mapped all business requirements from the ticket to specific unit test cases (focusing on logic testable in isolation)
- I am using `data-testid` for all element selections
- I am aiming for 90% code coverage (all critical branches, lines, functions)
- I am using `waitFor()` for async operations (never `setTimeout`)
- I am using `@testing-library/user-event` for user interactions
- I am creating minimal mocks that return only what's needed
- I am using `beforeEach`/`afterEach` for cleanup
- I am not testing third-party library internals
- I am using Given-When-Then structure with comments
- I am testing behavior, not implementation details
- I am using spies to verify callbacks and prop passing
- I am validating `data-track` attributes if present
- My test names are descriptive: `should <behavior> when <condition>`

---

## Pre-Implementation Checklist

### Using This Checklist Effectively

**CRITICAL RULE**: This checklist consolidates ALL requirements from this document - review before writing any frontend unit test.

**Examples**:

Checklist workflow:
1. **Before starting**: Review entire checklist to understand full scope
2. **During implementation**: Check off items as you complete them
3. **Before code review**: Verify all items are checked
4. **During review**: Confirm all items are actually implemented
5. **If item cannot be checked**: Review the relevant section above for guidance

### Required Verifications

### Test Framework Setup
- [ ] Using Vitest or Jest as test framework
- [ ] Using React Testing Library for component testing
- [ ] Using `@testing-library/user-event` for user interactions
- [ ] Using `@testing-library/jest-dom` for extended matchers
- [ ] Test file ends with `.unit.spec.tsx`, `.spec.tsx`, or `.spec.ts`
- [ ] Test file is co-located with component or in `__tests__` directory

### Element Selection
- [ ] Using `data-testid` for all element selections
- [ ] NOT using text content for element selection
- [ ] NOT using CSS selectors or class names for selection
- [ ] All testable elements have `data-testid` attributes in component
- [ ] NOT testing `data-testid` prop passing to child components

### Test Coverage
- [ ] All business logic and functional requirements from ticket are mapped to test cases
- [ ] Aiming for 90% code coverage (statements, branches, functions, lines)
- [ ] Testing all critical conditional rendering paths
- [ ] Testing all user interaction paths
- [ ] Testing loading, error, and success states
- [ ] Testing edge cases (empty, null, invalid inputs)
- [ ] NOT testing constants without logic

### Test Structure
- [ ] Test names follow `should <behavior> when <condition>` pattern
- [ ] Each test uses Given-When-Then structure with comments
- [ ] Tests focus on behavior, not implementation details
- [ ] Each test is focused on one specific scenario
- [ ] Tests are independent with no shared state

### Async Operations
- [ ] Using `waitFor()` for async operations
- [ ] Using `findBy` queries for async element appearance
- [ ] NOT using `setTimeout` or static waits
- [ ] NOT using arbitrary delays

### Mocking Strategy
- [ ] Mocking external APIs and third-party libraries
- [ ] Mocking child components for true unit test isolation
- [ ] Creating minimal mocks (only returning what's needed)
- [ ] NOT creating complex mock implementations
- [ ] NOT mocking internal utility functions
- [ ] NOT mocking React or React Testing Library
- [ ] Using spies (`vi.fn()`) to verify function calls

### User Interactions
- [ ] Using `@testing-library/user-event` for interactions
- [ ] NOT using `fireEvent` (unless userEvent doesn't support the event)
- [ ] Testing realistic user workflows
- [ ] Using `await` with user interactions

### Cleanup and Setup
- [ ] Using `beforeEach` to clear mocks and reset state
- [ ] Using `afterEach` to restore mocks and clean up side effects
- [ ] NOT letting mocks persist between tests
- [ ] Each test starts with clean slate

### Best Practices
- [ ] Validating `data-track` attributes if present
- [ ] NOT testing private implementation details
- [ ] Testing only through public APIs (never using bracket notation for private methods)
- [ ] Extracting complex private logic to testable utility functions when needed
- [ ] Using spies to verify prop passing to child components
- [ ] NOT writing meaningless assertions
- [ ] Following examples from project's existing test patterns

### Third-Party Libraries
- [ ] Mocking third-party libraries (Zod, analytics, etc.)
- [ ] Verifying library functions are called with correct parameters
- [ ] NOT testing library internals or validation logic
- [ ] Keeping mock implementations simple

---

## References

- **React Testing Library Documentation**: https://testing-library.com/docs/react-testing-library/intro/
- **Vitest Documentation**: https://vitest.dev/
- **Jest Documentation**: https://jestjs.io/
- **Testing Library User Event**: https://testing-library.com/docs/user-event/intro
- **Testing Library Cheatsheet**: https://testing-library.com/docs/react-testing-library/cheatsheet
- **Common Testing Mistakes**: https://kentcdodds.com/blog/common-mistakes-with-react-testing-library
