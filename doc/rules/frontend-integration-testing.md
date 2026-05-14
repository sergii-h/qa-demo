---
type: testing-standards
technology: react, typescript, javascript
test-type: integration-testing
frameworks: vitest, jest, react-testing-library
audience: developers, ai-assistants
applies-to: frontend, react-applications
---

# Frontend Integration Testing Standards

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
- **While writing tests**: Reference specific sections (Form Testing, Component Integration, etc.)
- **During code review**: Check "Common Pitfalls to Avoid" → Verify against "AI Assistant Instructions"
- **For troubleshooting**: Use section titles to find relevant patterns and examples

---

## Quick Reference Patterns

### Standard Integration Test Setup

**CRITICAL RULE**: Integration tests must render multiple components together and test real user workflows with minimal mocking.

**Examples**:

```typescript
// ✅ Correct: Integration test with real component interactions
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { RegistrationFlow } from './RegistrationFlow';
import { renderWithIntl } from '@shared/utils/test';

describe('RegistrationFlow Integration', () => {
  const mockApiSubmit = vi.fn();
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should complete registration with all required data', async () => {
    // Given: User starts registration flow
    const user = userEvent.setup();
    mockApiSubmit.mockResolvedValue({ success: true, userId: 'user-123' });
    
    renderWithIntl(<RegistrationFlow onSubmit={mockApiSubmit} />);
    
    // When: User fills out all steps
    // Step 1: Personal info
    await user.type(screen.getByTestId('email-input'), 'john@example.com');
    await user.type(screen.getByTestId('password-input'), 'SecurePass123!');
    await user.click(screen.getByTestId('next-button'));
    
    // Step 2: Profile details
    await waitFor(() => {
      expect(screen.getByTestId('profile-form')).toBeInTheDocument();
    });
    await user.type(screen.getByTestId('first-name-input'), 'John');
    await user.type(screen.getByTestId('last-name-input'), 'Doe');
    await user.click(screen.getByTestId('next-button'));
    
    // Step 3: Confirmation
    await waitFor(() => {
      expect(screen.getByTestId('confirmation-screen')).toBeInTheDocument();
    });
    await user.click(screen.getByTestId('confirm-button'));
    
    // Then: Registration completed successfully
    await waitFor(() => {
      expect(mockApiSubmit).toHaveBeenCalledWith({
        email: 'john@example.com',
        password: 'SecurePass123!',
        firstName: 'John',
        lastName: 'Doe',
      });
    });
    expect(screen.getByTestId('success-message')).toBeInTheDocument();
  });
});
```

### Essential Do's and Don'ts

**MANDATORY**: Follow these core practices for all frontend integration tests.

**Examples**:

✅ **DO**:
- Test complete user workflows and component interactions
- Keep project dependencies real (render actual child components)
- Mock only third-party dependencies and external APIs
- Test realistic user scenarios with multiple steps
- Use `waitFor()` for async operations (never `setTimeout`)
- Use `beforeEach`/`afterEach` for cleanup
- Use `data-testid` for element selection
- Test form submissions and multi-step flows
- Validate error handling and edge cases

❌ **DON'T**:
- Mock internal project components (use real ones)
- Create complex mock implementations
- Test logic already covered in unit tests (avoid duplication)
- Use static waits like `setTimeout` (use `waitFor()`)
- Use text content for element selection (use `data-testid`)
- Test in isolation (that's unit testing)
- Ignore cleanup between tests
- Test constants without logic

---

## Key Principles

### Focus on Component Interactions

**CRITICAL RULE**: Integration tests verify that multiple components work together correctly in realistic user scenarios.

**Examples**:

Integration testing scope:
- **Component combinations**: Multiple components rendering and interacting together
- **User workflows**: Complete multi-step user journeys
- **Data flow**: Props passing through component hierarchy
- **State management**: Shared state across components
- **Form flows**: Single-page and multi-step forms
- **Navigation**: Routing between views and preserving state
- **API integration**: Real API calls with mocked backend responses

### Minimal Mocking Strategy

**MANDATORY**: Keep project dependencies real - mock only third-party services and external APIs.

**Examples**:

Mock only these:
- **External APIs**: HTTP requests to backend services
- **Third-party services**: Analytics, payment processors, external SDKs
- **Browser APIs**: localStorage, sessionStorage (when needed)
- **External auth providers**: OAuth, social login

Keep these REAL:
- **Project components**: All internal React components
- **Project utilities**: Your own helper functions
- **Project hooks**: Custom React hooks
- **Context providers**: Your app's context providers
- **State management**: Redux, Zustand, or other state libraries
- **Routing**: React Router or similar (use MemoryRouter for tests)

### Test Complete User Workflows

**RULE**: Test realistic user scenarios from start to finish, including navigation and state preservation.

**Examples**:

Complete workflow testing:
- **Registration flow**: From landing page through all steps to confirmation
- **Checkout process**: Cart → shipping → payment → confirmation
- **Settings update**: Navigate to settings, change values, save, verify
- **Search and filter**: Enter search, apply filters, view results
- **Multi-step forms**: Navigate between steps, preserve data, handle errors

### Avoid Duplicating Unit Tests

**MANDATORY**: Don't re-test logic already covered in unit tests - focus on integration points.

**Examples**:

```typescript
// ❌ Bad: Testing validation logic in integration test (unit test concern)
it('should reject invalid email format', async () => {
  const user = userEvent.setup();
  renderWithIntl(<RegistrationForm />);
  
  await user.type(screen.getByTestId('email-input'), 'invalid-email');
  await user.click(screen.getByTestId('submit-button'));
  
  expect(screen.getByTestId('email-error')).toBeInTheDocument();
});

// ✅ Good: Testing complete registration flow (integration)
it('should complete registration with valid data through all steps', async () => {
  const user = userEvent.setup();
  renderWithIntl(<RegistrationFlow />);
  
  // Test multi-step navigation and data preservation
  await user.type(screen.getByTestId('email-input'), 'john@example.com');
  await user.click(screen.getByTestId('next-button'));
  
  await waitFor(() => {
    expect(screen.getByTestId('profile-step')).toBeInTheDocument();
  });
  // Continue testing workflow...
});
```

---

## Framework and Tools

### Testing Framework Stack

**MANDATORY**: Use these frameworks for frontend integration testing.

**Examples**:

Required frameworks:
- **Vitest or Jest**: Testing framework and test runner
- **React Testing Library**: Component rendering and interaction testing
- **@testing-library/user-event**: Realistic user interaction simulation
- **@testing-library/jest-dom**: Extended DOM matchers
- **MSW (Mock Service Worker)**: Mock API requests at network level
- **React Router Test Utils**: MemoryRouter for navigation testing

### File Naming and Location

**MANDATORY**: Follow consistent naming conventions to distinguish integration tests from unit tests.

**Examples**:

Test file naming:
- Integration test files must end with `.integration.spec.tsx`
- Pattern: `<FlowName>.integration.spec.tsx`
- Examples: `registration-flow.integration.spec.tsx`, `checkout-process.integration.spec.tsx`

Location:
- Place integration tests near the feature they test
- Alternative: Use `__tests__/integration/` directory
- Keep integration tests separate from unit tests

Test method naming:
- Use descriptive names: `should <complete workflow> when <condition>`
- Examples:
  - `should complete registration with all required steps`
  - `should preserve form data when navigating between steps`
  - `should handle API error during checkout process`

---

## Form Testing Patterns

### Standalone Form Testing

**RULE**: Test single-page forms with all data scenarios and error handling.

**Examples**:

```typescript
describe('Settings Form Integration', () => {
  const mockUpdateSettings = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should render with all user data and save changes', async () => {
    // Given: User with full profile data
    const user = userEvent.setup();
    const userData = {
      email: 'john@example.com',
      firstName: 'John',
      lastName: 'Doe',
      phone: '+1234567890',
      notificationsEnabled: true,
      newsletter: true,
    };
    mockUpdateSettings.mockResolvedValue({ success: true });

    renderWithIntl(
      <SettingsForm 
        initialData={userData} 
        onSave={mockUpdateSettings} 
      />
    );

    // When: User updates settings
    await user.clear(screen.getByTestId('phone-input'));
    await user.type(screen.getByTestId('phone-input'), '+9876543210');
    await user.click(screen.getByTestId('newsletter-checkbox')); // Uncheck
    await user.click(screen.getByTestId('save-button'));

    // Then: Settings saved with updated data
    await waitFor(() => {
      expect(mockUpdateSettings).toHaveBeenCalledWith({
        ...userData,
        phone: '+9876543210',
        newsletter: false,
      });
    });
    expect(screen.getByTestId('success-message')).toBeInTheDocument();
  });

  it('should render with minimal data when user profile incomplete', async () => {
    // Given: User with minimal data
    const userData = {
      email: 'john@example.com',
      firstName: 'John',
    };

    renderWithIntl(
      <SettingsForm 
        initialData={userData} 
        onSave={mockUpdateSettings} 
      />
    );

    // Then: Form renders with empty optional fields
    expect(screen.getByTestId('email-input')).toHaveValue('john@example.com');
    expect(screen.getByTestId('first-name-input')).toHaveValue('John');
    expect(screen.getByTestId('last-name-input')).toHaveValue('');
    expect(screen.getByTestId('phone-input')).toHaveValue('');
  });

  it('should handle API error when saving fails', async () => {
    // Given: API will fail
    const user = userEvent.setup();
    mockUpdateSettings.mockRejectedValue(new Error('Network error'));

    renderWithIntl(<SettingsForm initialData={mockData} onSave={mockUpdateSettings} />);

    // When: User attempts to save
    await user.type(screen.getByTestId('phone-input'), '+1234567890');
    await user.click(screen.getByTestId('save-button'));

    // Then: Error message displayed
    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
    });
    expect(screen.getByTestId('save-button')).not.toBeDisabled();
  });
});
```

### Multistep Form Testing

**MANDATORY**: Test multistep forms with navigation, data preservation, and error handling.

**Examples**:

```typescript
describe('Registration Flow Integration', () => {
  const mockRegisterUser = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should complete registration with all data through all steps', async () => {
    // Given: Registration flow with API mock
    const user = userEvent.setup();
    mockRegisterUser.mockResolvedValue({ 
      success: true, 
      userId: 'user-123' 
    });

    renderWithIntl(
      <RegistrationFlow onSubmit={mockRegisterUser} />
    );

    // When: User completes Step 1 - Account
    await user.type(screen.getByTestId('email-input'), 'john@example.com');
    await user.type(screen.getByTestId('password-input'), 'SecurePass123!');
    await user.type(screen.getByTestId('password-confirm-input'), 'SecurePass123!');
    await user.click(screen.getByTestId('terms-checkbox'));
    await user.click(screen.getByTestId('next-button'));

    // And: User completes Step 2 - Profile
    await waitFor(() => {
      expect(screen.getByTestId('profile-step')).toBeInTheDocument();
    });
    await user.type(screen.getByTestId('first-name-input'), 'John');
    await user.type(screen.getByTestId('last-name-input'), 'Doe');
    await user.type(screen.getByTestId('phone-input'), '+1234567890');
    await user.click(screen.getByTestId('next-button'));

    // And: User completes Step 3 - Preferences
    await waitFor(() => {
      expect(screen.getByTestId('preferences-step')).toBeInTheDocument();
    });
    await user.click(screen.getByTestId('newsletter-checkbox'));
    await user.click(screen.getByTestId('notifications-checkbox'));
    await user.click(screen.getByTestId('submit-button'));

    // Then: Registration submitted with all data
    await waitFor(() => {
      expect(mockRegisterUser).toHaveBeenCalledWith({
        email: 'john@example.com',
        password: 'SecurePass123!',
        firstName: 'John',
        lastName: 'Doe',
        phone: '+1234567890',
        newsletter: true,
        notifications: true,
        acceptedTerms: true,
      });
    });
    expect(screen.getByTestId('success-screen')).toBeInTheDocument();
  });

  it('should complete registration with minimal required data', async () => {
    // Given: Registration flow
    const user = userEvent.setup();
    mockRegisterUser.mockResolvedValue({ success: true });

    renderWithIntl(<RegistrationFlow onSubmit={mockRegisterUser} />);

    // When: User fills only required fields
    await user.type(screen.getByTestId('email-input'), 'jane@example.com');
    await user.type(screen.getByTestId('password-input'), 'Pass123!');
    await user.type(screen.getByTestId('password-confirm-input'), 'Pass123!');
    await user.click(screen.getByTestId('terms-checkbox'));
    await user.click(screen.getByTestId('next-button'));

    await waitFor(() => {
      expect(screen.getByTestId('profile-step')).toBeInTheDocument();
    });
    await user.type(screen.getByTestId('first-name-input'), 'Jane');
    await user.type(screen.getByTestId('last-name-input'), 'Smith');
    // Skip optional phone
    await user.click(screen.getByTestId('next-button'));

    await waitFor(() => {
      expect(screen.getByTestId('preferences-step')).toBeInTheDocument();
    });
    // Skip all preferences
    await user.click(screen.getByTestId('submit-button'));

    // Then: Registration succeeds with minimal data
    await waitFor(() => {
      expect(mockRegisterUser).toHaveBeenCalledWith({
        email: 'jane@example.com',
        password: 'Pass123!',
        firstName: 'Jane',
        lastName: 'Smith',
        newsletter: false,
        notifications: false,
        acceptedTerms: true,
      });
    });
  });

  it('should preserve data when navigating backward and forward', async () => {
    // Given: User enters data in step 1
    const user = userEvent.setup();
    renderWithIntl(<RegistrationFlow onSubmit={mockRegisterUser} />);

    // When: User fills step 1
    await user.type(screen.getByTestId('email-input'), 'test@example.com');
    await user.type(screen.getByTestId('password-input'), 'Password123!');
    await user.type(screen.getByTestId('password-confirm-input'), 'Password123!');
    await user.click(screen.getByTestId('terms-checkbox'));
    await user.click(screen.getByTestId('next-button'));

    // And: User fills step 2
    await waitFor(() => {
      expect(screen.getByTestId('profile-step')).toBeInTheDocument();
    });
    await user.type(screen.getByTestId('first-name-input'), 'Test');
    await user.type(screen.getByTestId('last-name-input'), 'User');

    // And: User navigates back to step 1
    await user.click(screen.getByTestId('back-button'));

    // Then: Step 1 data is preserved
    await waitFor(() => {
      expect(screen.getByTestId('account-step')).toBeInTheDocument();
    });
    expect(screen.getByTestId('email-input')).toHaveValue('test@example.com');
    expect(screen.getByTestId('password-input')).toHaveValue('Password123!');
    expect(screen.getByTestId('terms-checkbox')).toBeChecked();

    // When: User navigates forward again
    await user.click(screen.getByTestId('next-button'));

    // Then: Step 2 data is preserved
    await waitFor(() => {
      expect(screen.getByTestId('profile-step')).toBeInTheDocument();
    });
    expect(screen.getByTestId('first-name-input')).toHaveValue('Test');
    expect(screen.getByTestId('last-name-input')).toHaveValue('User');
  });

  it('should handle API error during final submission', async () => {
    // Given: API will reject submission
    const user = userEvent.setup();
    mockRegisterUser.mockRejectedValue(new Error('Email already exists'));

    renderWithIntl(<RegistrationFlow onSubmit={mockRegisterUser} />);

    // When: User completes all steps
    await user.type(screen.getByTestId('email-input'), 'existing@example.com');
    await user.type(screen.getByTestId('password-input'), 'Pass123!');
    await user.type(screen.getByTestId('password-confirm-input'), 'Pass123!');
    await user.click(screen.getByTestId('terms-checkbox'));
    await user.click(screen.getByTestId('next-button'));

    await waitFor(() => {
      expect(screen.getByTestId('profile-step')).toBeInTheDocument();
    });
    await user.type(screen.getByTestId('first-name-input'), 'John');
    await user.type(screen.getByTestId('last-name-input'), 'Doe');
    await user.click(screen.getByTestId('next-button'));

    await waitFor(() => {
      expect(screen.getByTestId('preferences-step')).toBeInTheDocument();
    });
    await user.click(screen.getByTestId('submit-button'));

    // Then: Error displayed and user can retry
    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('Email already exists');
    });
    expect(screen.getByTestId('submit-button')).not.toBeDisabled();
  });
});
```

---

## Component Integration Testing

### Testing Component Hierarchies

**RULE**: Test parent-child component interactions with real implementations.

**Examples**:

```typescript
describe('ProductList with ProductCard Integration', () => {
  const mockOnAddToCart = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render multiple products and handle add to cart', async () => {
    // Given: List of products
    const user = userEvent.setup();
    const products = [
      { id: '1', name: 'Laptop', price: 999.99, inStock: true },
      { id: '2', name: 'Mouse', price: 29.99, inStock: true },
      { id: '3', name: 'Keyboard', price: 79.99, inStock: false },
    ];

    renderWithIntl(
      <ProductList 
        products={products} 
        onAddToCart={mockOnAddToCart} 
      />
    );

    // When: User adds first product to cart
    const addButtons = screen.getAllByTestId('add-to-cart-button');
    await user.click(addButtons[0]);

    // Then: Callback invoked with correct product
    expect(mockOnAddToCart).toHaveBeenCalledWith({ id: '1', name: 'Laptop', price: 999.99 });

    // And: Out of stock product button is disabled
    expect(addButtons[2]).toBeDisabled();
  });

  it('should filter products based on search input', async () => {
    // Given: Products with search functionality
    const user = userEvent.setup();
    const products = [
      { id: '1', name: 'Laptop', price: 999.99 },
      { id: '2', name: 'Mouse', price: 29.99 },
      { id: '3', name: 'Keyboard', price: 79.99 },
    ];

    renderWithIntl(<ProductList products={products} />);

    // When: User searches for "laptop"
    await user.type(screen.getByTestId('search-input'), 'laptop');

    // Then: Only matching product displayed
    await waitFor(() => {
      expect(screen.getByTestId('product-card-1')).toBeInTheDocument();
      expect(screen.queryByTestId('product-card-2')).not.toBeInTheDocument();
      expect(screen.queryByTestId('product-card-3')).not.toBeInTheDocument();
    });
  });
});
```

### Testing with Context Providers

**RULE**: Test components with real context providers to verify state management integration.

**Examples**:

```typescript
describe('ShoppingCart with CartContext Integration', () => {
  it('should add item to cart and update cart count', async () => {
    // Given: Cart with context provider
    const user = userEvent.setup();
    const product = { id: 'prod-1', name: 'Laptop', price: 999.99 };

    renderWithIntl(
      <CartProvider>
        <ProductCard product={product} />
        <CartSummary />
      </CartProvider>
    );

    // When: User adds product to cart
    await user.click(screen.getByTestId('add-to-cart-button'));

    // Then: Cart count updated across components
    await waitFor(() => {
      expect(screen.getByTestId('cart-count')).toHaveTextContent('1');
      expect(screen.getByTestId('cart-total')).toHaveTextContent('$999.99');
    });
  });

  it('should remove item from cart and update totals', async () => {
    // Given: Cart with items
    const user = userEvent.setup();
    const initialCart = [
      { id: 'prod-1', name: 'Laptop', price: 999.99, quantity: 1 },
      { id: 'prod-2', name: 'Mouse', price: 29.99, quantity: 2 },
    ];

    renderWithIntl(
      <CartProvider initialCart={initialCart}>
        <CartItemList />
        <CartSummary />
      </CartProvider>
    );

    // When: User removes first item
    const removeButtons = screen.getAllByTestId('remove-item-button');
    await user.click(removeButtons[0]);

    // Then: Cart updated
    await waitFor(() => {
      expect(screen.getByTestId('cart-count')).toHaveTextContent('2');
      expect(screen.getByTestId('cart-total')).toHaveTextContent('$59.98');
    });
  });
});
```

---

## Testing Navigation and Routing

### Testing Route Navigation

**RULE**: Use MemoryRouter to test navigation between routes and state preservation.

**Examples**:

```typescript
import { MemoryRouter, Route, Routes } from 'react-router-dom';

describe('User Profile Navigation Integration', () => {
  it('should navigate to edit profile and back', async () => {
    // Given: Profile view with routing
    const user = userEvent.setup();
    const userData = { id: 'user-1', name: 'John Doe', email: 'john@example.com' };

    renderWithIntl(
      <MemoryRouter initialEntries={['/profile']}>
        <Routes>
          <Route path="/profile" element={<ProfileView user={userData} />} />
          <Route path="/profile/edit" element={<ProfileEdit user={userData} />} />
        </Routes>
      </MemoryRouter>
    );

    // When: User clicks edit button
    await user.click(screen.getByTestId('edit-profile-button'));

    // Then: Navigated to edit page
    await waitFor(() => {
      expect(screen.getByTestId('profile-edit-form')).toBeInTheDocument();
    });

    // When: User cancels edit
    await user.click(screen.getByTestId('cancel-button'));

    // Then: Back to profile view
    await waitFor(() => {
      expect(screen.getByTestId('profile-view')).toBeInTheDocument();
    });
  });

  it('should preserve form data when navigating away and returning', async () => {
    // Given: Edit form with navigation
    const user = userEvent.setup();

    renderWithIntl(
      <MemoryRouter initialEntries={['/profile/edit']}>
        <Routes>
          <Route path="/profile/edit" element={<ProfileEdit />} />
          <Route path="/profile" element={<ProfileView />} />
        </Routes>
      </MemoryRouter>
    );

    // When: User enters data but doesn't save
    await user.type(screen.getByTestId('name-input'), 'Jane Doe');
    
    // And: Navigates away (with unsaved changes warning)
    const navigateLink = screen.getByTestId('profile-link');
    await user.click(navigateLink);

    // Then: Unsaved changes warning shown
    await waitFor(() => {
      expect(screen.getByTestId('unsaved-changes-modal')).toBeInTheDocument();
    });
  });
});
```

---

## Testing API Integration

### Mocking API Calls with MSW

**RULE**: Use Mock Service Worker (MSW) to mock API calls at the network level for realistic integration testing.

**Examples**:

```typescript
import { rest } from 'msw';
import { setupServer } from 'msw/node';

const server = setupServer(
  rest.post('/api/users/register', (req, res, ctx) => {
    return res(ctx.json({ success: true, userId: 'user-123' }));
  }),
  rest.get('/api/users/:userId', (req, res, ctx) => {
    const { userId } = req.params;
    return res(ctx.json({ 
      id: userId, 
      name: 'John Doe', 
      email: 'john@example.com' 
    }));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('User Profile with API Integration', () => {
  it('should fetch and display user profile', async () => {
    // Given: User profile component
    renderWithIntl(<UserProfile userId="user-123" />);

    // When: Component fetches data
    // Then: Loading state shown initially
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();

    // And: User data displayed after fetch
    await waitFor(() => {
      expect(screen.getByTestId('user-name')).toHaveTextContent('John Doe');
      expect(screen.getByTestId('user-email')).toHaveTextContent('john@example.com');
    });
    expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument();
  });

  it('should handle API error gracefully', async () => {
    // Given: API will return error
    server.use(
      rest.get('/api/users/:userId', (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server error' }));
      })
    );

    renderWithIntl(<UserProfile userId="user-123" />);

    // Then: Error message displayed
    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
    });
  });

  it('should retry failed request when retry button clicked', async () => {
    // Given: API initially fails
    let callCount = 0;
    server.use(
      rest.get('/api/users/:userId', (req, res, ctx) => {
        callCount++;
        if (callCount === 1) {
          return res(ctx.status(500), ctx.json({ error: 'Server error' }));
        }
        return res(ctx.json({ id: 'user-123', name: 'John Doe' }));
      })
    );

    const user = userEvent.setup();
    renderWithIntl(<UserProfile userId="user-123" />);

    // When: Error displayed and user clicks retry
    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
    });
    await user.click(screen.getByTestId('retry-button'));

    // Then: Data loads successfully
    await waitFor(() => {
      expect(screen.getByTestId('user-name')).toHaveTextContent('John Doe');
    });
  });
});
```

---

## Async Operations and Loading States

### Testing Async Workflows

**CRITICAL RULE**: Use `waitFor()` for async operations - never use `setTimeout` or static waits.

**Examples**:

```typescript
describe('Checkout Process Integration', () => {
  it('should show loading state during payment processing', async () => {
    // Given: Checkout with delayed API response
    const user = userEvent.setup();
    const mockProcessPayment = vi.fn(() => 
      new Promise(resolve => setTimeout(() => resolve({ success: true }), 500))
    );

    renderWithIntl(
      <CheckoutFlow 
        cart={mockCart} 
        onProcessPayment={mockProcessPayment} 
      />
    );

    // When: User submits payment
    await user.type(screen.getByTestId('card-number-input'), '4111111111111111');
    await user.type(screen.getByTestId('cvv-input'), '123');
    await user.click(screen.getByTestId('submit-payment-button'));

    // Then: Loading state shown immediately
    expect(screen.getByTestId('processing-spinner')).toBeInTheDocument();
    expect(screen.getByTestId('submit-payment-button')).toBeDisabled();

    // And: Success screen shown after processing
    await waitFor(() => {
      expect(screen.getByTestId('payment-success')).toBeInTheDocument();
    });
    expect(screen.queryByTestId('processing-spinner')).not.toBeInTheDocument();
  });

  it('should handle slow API with timeout message', async () => {
    // Given: Very slow API
    const user = userEvent.setup();
    const mockProcessPayment = vi.fn(() => 
      new Promise(resolve => setTimeout(() => resolve({ success: true }), 10000))
    );

    renderWithIntl(<CheckoutFlow onProcessPayment={mockProcessPayment} />);

    // When: User submits
    await user.click(screen.getByTestId('submit-payment-button'));

    // Then: Timeout warning shown after threshold
    await waitFor(() => {
      expect(screen.getByTestId('slow-connection-warning')).toBeInTheDocument();
    }, { timeout: 6000 });
  });
});
```

---

## Best Practices

### Setup and Cleanup

**MANDATORY**: Use `beforeEach` and `afterEach` to ensure clean test isolation.

**Examples**:

```typescript
describe('Shopping Cart Integration', () => {
  let mockLocalStorage: { [key: string]: string };

  beforeEach(() => {
    // Clear all mocks
    vi.clearAllMocks();
    
    // Setup clean localStorage
    mockLocalStorage = {};
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: (key: string) => mockLocalStorage[key] || null,
        setItem: (key: string, value: string) => {
          mockLocalStorage[key] = value;
        },
        removeItem: (key: string) => {
          delete mockLocalStorage[key];
        },
        clear: () => {
          Object.keys(mockLocalStorage).forEach(key => delete mockLocalStorage[key]);
        },
      },
      writable: true,
    });
  });

  afterEach(() => {
    // Restore all mocks
    vi.restoreAllMocks();
    
    // Clear localStorage
    window.localStorage.clear();
  });

  it('should persist cart items to localStorage', async () => {
    const user = userEvent.setup();
    renderWithIntl(<ShoppingCart />);

    await user.click(screen.getByTestId('add-item-button'));

    expect(window.localStorage.setItem).toHaveBeenCalledWith(
      'cart',
      expect.stringContaining('item-1')
    );
  });
});
```

---

## Common Pitfalls to Avoid

### Understanding Anti-Patterns

**CRITICAL RULE**: Avoid these common mistakes that reduce integration test quality and effectiveness.

**Examples**:

Review these pitfalls regularly:
1. Mocking internal project components
2. Duplicating unit test logic
3. Using setTimeout for async operations
4. Complex mock implementations
5. Testing components in isolation (that's unit testing)
6. Using text content for element selection
7. Not cleaning up between tests
8. Testing constants without logic

---

### 1. Mocking Internal Project Components

❌ **DON'T**: Mock your own components - that defeats the purpose of integration testing.

```typescript
// ❌ Bad: Mocking internal components
vi.mock('./UserCard', () => ({
  UserCard: ({ user }) => <div data-testid="user-card-mock">{user.name}</div>
}));

describe('UserList Integration', () => {
  it('should render user list', () => {
    renderWithIntl(<UserList users={mockUsers} />);
    // This is not testing real integration!
  });
});
```

✅ **DO**: Use real internal components to test actual integration.

```typescript
// ✅ Good: Using real components
describe('UserList Integration', () => {
  it('should render user cards with real UserCard component', () => {
    // Given: List with users
    const users = [
      { id: '1', name: 'John Doe', email: 'john@example.com' },
      { id: '2', name: 'Jane Smith', email: 'jane@example.com' },
    ];

    // When: Component renders with real child components
    renderWithIntl(<UserList users={users} />);

    // Then: All user cards rendered with full functionality
    expect(screen.getByTestId('user-card-1')).toBeInTheDocument();
    expect(screen.getByTestId('user-card-2')).toBeInTheDocument();
    expect(screen.getByTestId('user-name-1')).toHaveTextContent('John Doe');
    expect(screen.getByTestId('user-email-1')).toHaveTextContent('john@example.com');
  });
});
```

### 2. Duplicating Unit Test Logic

❌ **DON'T**: Re-test validation and business logic already covered in unit tests.

```typescript
// ❌ Bad: Testing validation in integration test
describe('Registration Form Integration', () => {
  it('should reject invalid email', async () => {
    const user = userEvent.setup();
    renderWithIntl(<RegistrationForm />);
    
    await user.type(screen.getByTestId('email-input'), 'invalid');
    await user.click(screen.getByTestId('submit-button'));
    
    expect(screen.getByTestId('email-error')).toBeInTheDocument();
  });

  it('should reject short password', async () => {
    // Testing validation logic again - already in unit tests!
  });
});
```

✅ **DO**: Focus on multi-step workflows and component interactions.

```typescript
// ✅ Good: Testing complete workflow integration
describe('Registration Flow Integration', () => {
  it('should complete multi-step registration with data preservation', async () => {
    const user = userEvent.setup();
    renderWithIntl(<RegistrationFlow />);
    
    // Test multi-step navigation and data flow
    await user.type(screen.getByTestId('email-input'), 'john@example.com');
    await user.type(screen.getByTestId('password-input'), 'SecurePass123!');
    await user.click(screen.getByTestId('next-button'));
    
    // Verify step 2 renders and step 1 data preserved
    await waitFor(() => {
      expect(screen.getByTestId('profile-step')).toBeInTheDocument();
    });
    
    await user.click(screen.getByTestId('back-button'));
    
    // Verify data preserved when navigating back
    expect(screen.getByTestId('email-input')).toHaveValue('john@example.com');
  });
});
```

### 3. Using setTimeout for Async Operations

❌ **DON'T**: Use setTimeout or arbitrary waits - tests become slow and flaky.

```typescript
// ❌ Bad: Using setTimeout
it('should load data after API call', async () => {
  renderWithIntl(<Dashboard />);
  
  await new Promise(resolve => setTimeout(resolve, 2000)); // Slow and unreliable!
  
  expect(screen.getByTestId('data-table')).toBeInTheDocument();
});
```

✅ **DO**: Use waitFor() to wait for actual conditions.

```typescript
// ✅ Good: Using waitFor
it('should load data after API call', async () => {
  renderWithIntl(<Dashboard />);
  
  await waitFor(() => {
    expect(screen.getByTestId('data-table')).toBeInTheDocument();
  });
});

// ✅ Good: Using findBy (built-in waiting)
it('should load data after API call', async () => {
  renderWithIntl(<Dashboard />);
  
  const dataTable = await screen.findByTestId('data-table');
  expect(dataTable).toBeInTheDocument();
});
```

### 4. Complex Mock Implementations

❌ **DON'T**: Create complex mocks with lots of logic.

```typescript
// ❌ Bad: Complex mock implementation
const mockApi = vi.fn().mockImplementation(async (endpoint, data) => {
  if (endpoint === '/users') {
    if (!data.email) throw new Error('Email required');
    if (data.email === 'admin@test.com') {
      return { id: 'admin', role: 'admin', permissions: [...], settings: {...} };
    }
    const userId = `user-${Date.now()}`;
    return {
      id: userId,
      email: data.email,
      role: 'user',
      createdAt: new Date().toISOString(),
      // Lots of complex logic...
    };
  }
  throw new Error('Unknown endpoint');
});
```

✅ **DO**: Create minimal mocks that return only what the test needs.

```typescript
// ✅ Good: Minimal mock per test
it('should register user successfully', async () => {
  const mockRegisterApi = vi.fn().mockResolvedValue({
    success: true,
    userId: 'user-123',
  });
  
  const user = userEvent.setup();
  renderWithIntl(<RegistrationForm onSubmit={mockRegisterApi} />);
  
  // Test continues...
});
```

### 5. Testing Components in Isolation

❌ **DON'T**: Test single components in isolation - that's unit testing.

```typescript
// ❌ Bad: Testing single component (this is unit testing)
describe('UserCard Integration', () => {
  it('should render user card', () => {
    const user = { id: '1', name: 'John' };
    renderWithIntl(<UserCard user={user} />);
    
    expect(screen.getByTestId('user-name')).toHaveTextContent('John');
  });
});
```

✅ **DO**: Test multiple components working together.

```typescript
// ✅ Good: Testing component integration
describe('UserList with UserCard Integration', () => {
  it('should render list of user cards with interactions', async () => {
    const user = userEvent.setup();
    const users = [
      { id: '1', name: 'John' },
      { id: '2', name: 'Jane' },
    ];
    const onSelectUser = vi.fn();

    renderWithIntl(
      <UserList users={users} onSelectUser={onSelectUser} />
    );

    // Test parent-child interaction
    const firstUserCard = screen.getByTestId('user-card-1');
    await user.click(firstUserCard);
    
    expect(onSelectUser).toHaveBeenCalledWith({ id: '1', name: 'John' });
  });
});
```

### 6. Using Text Content for Selection

❌ **DON'T**: Select elements by text - it's fragile and locale-dependent.

```typescript
// ❌ Bad: Selecting by text
it('should show success message', async () => {
  renderWithIntl(<RegistrationForm />);
  
  // Text might change with translations or copy updates!
  expect(screen.getByText('Registration successful')).toBeInTheDocument();
});
```

✅ **DO**: Always use data-testid for reliable element selection.

```typescript
// ✅ Good: Using data-testid
it('should show success message', async () => {
  renderWithIntl(<RegistrationForm />);
  
  expect(screen.getByTestId('success-message')).toBeInTheDocument();
});
```

### 7. Not Cleaning Up Between Tests

❌ **DON'T**: Let state, mocks, or side effects persist between tests.

```typescript
// ❌ Bad: No cleanup
describe('Cart Integration', () => {
  it('should add item to cart', async () => {
    renderWithIntl(<Cart />);
    // Add item...
    // No cleanup!
  });

  it('should remove item from cart', async () => {
    // Previous test's cart state might affect this test!
  });
});
```

✅ **DO**: Use beforeEach/afterEach for proper cleanup.

```typescript
// ✅ Good: Proper cleanup
describe('Cart Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should add item to cart', async () => {
    renderWithIntl(<Cart />);
    // Test...
  });

  it('should remove item from cart', async () => {
    // Clean slate from beforeEach
    renderWithIntl(<Cart />);
    // Test...
  });
});
```

### 8. Testing Constants Without Logic

❌ **DON'T**: Test constants or configuration values.

```typescript
// ❌ Bad: Testing constants
it('should have correct button label', () => {
  renderWithIntl(<SubmitButton />);
  expect(BUTTON_LABEL).toBe('Submit');
});
```

✅ **DO**: Test logic and behavior.

```typescript
// ✅ Good: Testing behavior
it('should disable button while form is submitting', async () => {
  const user = userEvent.setup();
  renderWithIntl(<SubmitForm onSubmit={mockSubmit} />);
  
  await user.click(screen.getByTestId('submit-button'));
  
  expect(screen.getByTestId('submit-button')).toBeDisabled();
});
```

---

## AI Assistant Instructions

### Before Generating Any Test Code

**CRITICAL RULE**: AI assistants must complete this preparation before generating any frontend integration test code.

**Examples**:

1. **Read the Pre-Implementation Checklist** at the end of this document
2. **Review "Common Pitfalls to Avoid"** section to avoid anti-patterns
3. **Understand form testing patterns** - standalone vs multistep
4. **Use "Quick Reference Patterns"** section for standard templates
5. **Verify integration scope** - ensure testing multiple components together

### Mandatory Practices for AI-Generated Tests

**CRITICAL RULE**: Every frontend integration test generated by AI assistants must follow these practices without exception.

**Examples**:

1. **Use real project components**: Never mock internal components - use actual implementations.

2. **Test complete workflows**: Focus on multi-step user journeys, not isolated component behavior.

3. **Mock only external dependencies**: Mock APIs and third-party services, keep project code real.

4. **Use minimal mocks**: Keep mocks simple - only return what the test needs.

5. **Follow form testing patterns**: Use established patterns for standalone and multistep forms.

6. **Use waitFor() for async**: Never use `setTimeout` - use `waitFor()` or `findBy` queries.

7. **Use data-testid**: Always select elements by `data-testid`, never by text.

8. **Setup and cleanup**: Use `beforeEach`/`afterEach` for proper test isolation.

9. **Don't duplicate unit tests**: Skip validation and business logic - focus on integration.

10. **Test data preservation**: Verify state persists when navigating between steps/routes.

11. **Test error scenarios**: Include API error handling and edge cases.

12. **Use MSW for API mocking**: Mock network requests with Mock Service Worker when applicable.

13. **Test with context providers**: Use real context providers to test state management.

### Quick Decision Trees for AI Assistants

**RULE**: Use these decision trees for quick answers when generating integration tests.

**Examples**:

**Q: Should I mock this dependency?**
- External API or backend service → YES, mock with MSW or vi.fn()
- Third-party service (analytics, payment) → YES, mock
- Internal project component → NO, use real component
- Internal custom hook → NO, use real hook
- Context provider → NO, use real provider

**Q: What type of integration test should I write?**
- Standalone single-page form → Use standalone form pattern (3 test cases)
- Multi-step form flow → Use multistep form pattern (4 test cases)
- Component hierarchy → Test parent-child interactions
- Navigation flow → Use MemoryRouter, test route changes
- API integration → Use MSW for network mocking

**Q: Is this integration testing or unit testing?**
- Testing single component in isolation → Unit test
- Testing multiple components together → Integration test
- Testing complete user workflow → Integration test
- Testing component with context/providers → Integration test
- Testing navigation between routes → Integration test

**Q: Should I test this in integration tests?**
- Complete user workflows → YES
- Multi-component interactions → YES
- Form submission flows → YES
- Navigation and routing → YES
- State management across components → YES
- Validation logic → NO (unit tests)
- Utility functions → NO (unit tests)

**Q: How should I handle async operations?**
- Use `waitFor()` for conditional waiting
- Use `findBy` queries (have built-in waiting)
- Never use `setTimeout` or `sleep`
- Test loading states before completion

### Final Verification Before Generating Tests

**CRITICAL RULE**: Verify these items immediately before generating any integration test code.

**Examples**:

✅ Checklist:
- I have reviewed the Pre-Implementation Checklist
- I am using real project components (not mocking them)
- I am testing complete workflows with multiple components
- I am mocking only external APIs and third-party services
- I am using minimal mocks that return only what's needed
- I am following form testing patterns (standalone or multistep)
- I am using `waitFor()` for async operations (never `setTimeout`)
- I am using `data-testid` for element selection
- I am using `beforeEach`/`afterEach` for cleanup
- I am not duplicating unit test logic
- I am testing data preservation in multi-step flows
- I am testing error scenarios and edge cases
- My test names are descriptive: `should <complete workflow> when <condition>`

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
- [ ] Using Vitest or Jest as test framework
- [ ] Using React Testing Library for rendering
- [ ] Using `@testing-library/user-event` for interactions
- [ ] Test file ends with `.integration.spec.tsx`
- [ ] Test file is placed appropriately in project structure

### Integration Scope
- [ ] Testing multiple components working together
- [ ] Testing complete user workflows (not isolated behavior)
- [ ] Using real project components (not mocking them)
- [ ] Testing realistic user scenarios with multiple steps
- [ ] NOT testing single components in isolation (that's unit testing)

### Mocking Strategy
- [ ] Mocking only external APIs and third-party services
- [ ] NOT mocking internal project components
- [ ] NOT mocking internal custom hooks
- [ ] NOT mocking context providers (using real ones)
- [ ] Creating minimal mocks (only returning what's needed)
- [ ] Using MSW for API mocking when applicable

### Form Testing
- [ ] Following standalone form pattern if single-page form
- [ ] Following multistep form pattern if multi-step form
- [ ] Testing with all data scenario
- [ ] Testing with minimal data scenario (if different from all data)
- [ ] Testing data preservation when navigating between steps (multistep)
- [ ] Testing API error scenarios

### Element Selection
- [ ] Using `data-testid` for all element selections
- [ ] NOT using text content for element selection
- [ ] NOT using CSS selectors or class names
- [ ] All testable elements have appropriate data-testid attributes

### Async Operations
- [ ] Using `waitFor()` for async operations
- [ ] Using `findBy` queries for async element appearance
- [ ] NOT using `setTimeout` or static waits
- [ ] Testing loading states before completion

### User Interactions
- [ ] Using `@testing-library/user-event` for interactions
- [ ] Testing realistic user workflows
- [ ] Using `await` with user interactions
- [ ] Testing multi-step interactions

### State and Navigation
- [ ] Testing state preservation across navigation
- [ ] Using MemoryRouter for route testing when applicable
- [ ] Testing with real context providers
- [ ] Verifying shared state across components

### Cleanup and Setup
- [ ] Using `beforeEach` to clear mocks and reset state
- [ ] Using `afterEach` to restore mocks and cleanup
- [ ] NOT letting state persist between tests
- [ ] Each test starts with clean slate

### Test Coverage
- [ ] NOT duplicating unit test logic
- [ ] NOT testing validation logic (unit test concern)
- [ ] Focusing on component interactions and workflows
- [ ] Testing error handling at integration level
- [ ] Testing complete scenarios, not edge cases

### Best Practices
- [ ] Following established project patterns
- [ ] Test names describe complete workflows
- [ ] Using Given-When-Then structure where appropriate
- [ ] NOT testing constants without logic
- [ ] Proper test organization and grouping

---

## References

- **React Testing Library Documentation**: https://testing-library.com/docs/react-testing-library/intro/
- **Vitest Documentation**: https://vitest.dev/
- **Testing Library User Event**: https://testing-library.com/docs/user-event/intro
- **Mock Service Worker (MSW)**: https://mswjs.io/
- **React Router Testing**: https://reactrouter.com/en/main/start/overview
- **Integration Testing Best Practices**: https://kentcdodds.com/blog/write-tests
