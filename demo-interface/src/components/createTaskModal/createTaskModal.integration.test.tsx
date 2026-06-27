import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TaskStatus, TaskPriority } from '../../interfaces/ITask';
import { TasksTable } from '../tasksTable/tasksTable';
import { mockFetchResponse } from '../../test-utils/mockFetch';

vi.stubGlobal('fetch', vi.fn());

describe('CreateTaskModal integration', () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe('Create task tests', () => {
    it('should create task with all values, send correct POST request and add new task to the list after successful response', async () => {
      // given
      const createTaskData = {
        id: 'task-123',
        title: 'Test Task',
        description: 'Test Description',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };

      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: [
            { body: [] },
            { body: [createTaskData] },
          ],
          POST: {
            body: createTaskData,
          },
        },
      });

      render(<TasksTable />);

      await user.click(screen.getByTestId('add-task-button'));

      await user.type(screen.getByTestId('create-task-title-input'), createTaskData.title);
      await user.type(screen.getByLabelText(/description/i), createTaskData.description);
      await user.click(screen.getByTestId('status-dropdown'));
      await user.click(screen.getByTestId(`status-dropdown-option-${createTaskData.status}`));
      await user.click(screen.getByTestId('priority-dropdown'));
      await user.click(screen.getByTestId(`priority-dropdown-option-${createTaskData.priority}`));

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('create-task-title-input')).not.toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${createTaskData.id}`)).toHaveTextContent(createTaskData.title);
      });

      expect(globalThis.fetch).toHaveBeenNthCalledWith(1,
        expect.stringContaining('/v1/tasks'),
        {
          method: 'GET',
          body: undefined,
          headers: { 'Content-Type': 'application/json' },
        },
      );

      expect(globalThis.fetch).toHaveBeenNthCalledWith(2,
        expect.stringContaining('/v1/tasks'),
        {
          method: 'POST',
          body: JSON.stringify({
            title: createTaskData.title,
            description: createTaskData.description,
            status: createTaskData.status,
            priority: createTaskData.priority,
          }),
          headers: { 'Content-Type': 'application/json' },
        },
      );

      expect(globalThis.fetch).toHaveBeenNthCalledWith(3,
        expect.stringContaining('/v1/tasks'),
        {
          method: 'GET',
          body: undefined,
          headers: { 'Content-Type': 'application/json' },
        },
      );
    });

    it('should create task with required values, send correct POST request and add new task to the list after successful response', async () => {
      // given
      const createTaskData = {
        id: 'task-124',
        title: 'Test Task',
        description: '',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };

      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: [
            { body: [] },
            { body: [createTaskData] },
          ],
          POST: {
            body: createTaskData,
          },
        },
      });

      render(<TasksTable />);

      await user.click(screen.getByTestId('add-task-button'));

      await user.type(screen.getByTestId('create-task-title-input'), createTaskData.title);
      await user.click(screen.getByTestId('status-dropdown'));
      await user.click(screen.getByTestId(`status-dropdown-option-${createTaskData.status}`));
      await user.click(screen.getByTestId('priority-dropdown'));
      await user.click(screen.getByTestId(`priority-dropdown-option-${createTaskData.priority}`));

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('create-task-title-input')).not.toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${createTaskData.id}`)).toHaveTextContent(createTaskData.title);
      });

      expect(globalThis.fetch).toHaveBeenNthCalledWith(2,
        expect.stringContaining('/v1/tasks'),
        {
          method: 'POST',
          body: JSON.stringify({
            title: createTaskData.title,
            description: createTaskData.description,
            status: createTaskData.status,
            priority: createTaskData.priority,
          }),
          headers: { 'Content-Type': 'application/json' },
        },
      );
    });

    it('should allow successful creation after invalid title is corrected', async () => {
      // given
      const createTaskData = {
        id: 'task-130',
        title: 'Corrected title',
        description: '',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };

      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: [
            { body: [] },
            { body: [createTaskData] },
          ],
          POST: {
            body: createTaskData,
            status: 201,
          },
        },
      });

      render(<TasksTable />);

      await user.click(screen.getByTestId('add-task-button'));
      await user.type(screen.getByTestId('create-task-title-input'), 'a'.repeat(101));

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toBeVisible();
      });
      expect(globalThis.fetch).toHaveBeenCalledTimes(1);

      // when
      await user.clear(screen.getByTestId('create-task-title-input'));
      await user.type(screen.getByTestId('create-task-title-input'), createTaskData.title);
      await user.click(screen.getByTestId('status-dropdown'));
      await user.click(screen.getByTestId(`status-dropdown-option-${createTaskData.status}`));
      await user.click(screen.getByTestId('priority-dropdown'));
      await user.click(screen.getByTestId(`priority-dropdown-option-${createTaskData.priority}`));
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('create-task-title-input')).not.toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${createTaskData.id}`)).toHaveTextContent(createTaskData.title);
      });
    });

    it('should not create a task when create form is closed without saving, and should reset form on reopen', async () => {
      // given
      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: { body: [] },
        },
      });

      render(<TasksTable />);

      await user.click(screen.getByTestId('add-task-button'));
      await user.type(screen.getByTestId('create-task-title-input'), 'Temporary title');
      await user.type(screen.getByLabelText(/description/i), 'Temporary description');

      // when
      await user.click(screen.getByTestId('close-button'));

      // then
      expect(screen.queryByText('Temporary title')).not.toBeInTheDocument();

      // when
      await user.click(screen.getByTestId('add-task-button'));

      // then
      expect(screen.getByTestId('create-task-title-input')).toHaveValue('');
      expect(screen.getByLabelText(/description/i)).toHaveValue('');
      expect(globalThis.fetch).toHaveBeenCalledTimes(1);
    });

    it('should allow retry and create task after initial POST failure', async () => {
      // given
      const createTaskData = {
        id: 'task-456',
        title: 'Retry Task',
        description: 'Retry Description',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };

      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: [
            { body: [] },
            { body: [createTaskData] },
          ],
          POST: [
            {
              body: null,
              status: 500,
            },
            {
              body: createTaskData,
              status: 201,
            },
          ],
        },
      });

      render(<TasksTable />);

      await user.click(screen.getByTestId('add-task-button'));
      await user.type(screen.getByTestId('create-task-title-input'), createTaskData.title);
      await user.type(screen.getByLabelText(/description/i), createTaskData.description);
      await user.click(screen.getByTestId('status-dropdown'));
      await user.click(screen.getByTestId(`status-dropdown-option-${createTaskData.status}`));
      await user.click(screen.getByTestId('priority-dropdown'));
      await user.click(screen.getByTestId(`priority-dropdown-option-${createTaskData.priority}`));

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toBeVisible();
      });
      expect(screen.getByTestId('create-task-title-input')).toBeVisible();

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('create-task-title-input')).not.toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${createTaskData.id}`)).toHaveTextContent(createTaskData.title);
      });
    });

    it.each([
      { testCase: 'HTTP 500', getResponse: { body: { message: 'Server error' }, status: 500 } },
      { testCase: 'network error', getResponse: { body: null, reject: 'Initial load failed' } },
    ])('should allow opening create form when initial GET tasks fails with $testCase', async ({ getResponse }) => {
      // given
      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: getResponse,
        },
      });

      render(<TasksTable />);

      // then
      await waitFor(() => {
        expect(screen.getByTestId('add-task-button')).toBeVisible();
      });
      expect(globalThis.fetch).toHaveBeenCalledTimes(1);

      // when
      await user.click(screen.getByTestId('add-task-button'));

      // then
      expect(screen.getByTestId('create-task-title-input')).toBeVisible();
    });

    it.each([
      { testCase: 'HTTP 500', refreshGetResponse: { body: null, status: 500 } },
      { testCase: 'network error', refreshGetResponse: { body: null, reject: 'Refresh failed' } },
    ])('should close create form when refresh GET fails with $testCase after successful POST', async ({ refreshGetResponse }) => {
      // given
      const createTaskData = {
        title: 'Task with refresh failure',
        description: '',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };

      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: [
            { body: [] },
            refreshGetResponse,
          ],
          POST: {
            body: {
              id: 'task-778',
              ...createTaskData,
            },
            status: 201,
          },
        },
      });

      render(<TasksTable />);

      await user.click(screen.getByTestId('add-task-button'));
      await user.type(screen.getByTestId('create-task-title-input'), createTaskData.title);
      await user.click(screen.getByTestId('status-dropdown'));
      await user.click(screen.getByTestId(`status-dropdown-option-${createTaskData.status}`));
      await user.click(screen.getByTestId('priority-dropdown'));
      await user.click(screen.getByTestId(`priority-dropdown-option-${createTaskData.priority}`));

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('create-task-title-input')).not.toBeInTheDocument();
      });
      expect(globalThis.fetch).toHaveBeenNthCalledWith(3,
        expect.stringContaining('/v1/tasks'),
        {
          method: 'GET',
          body: undefined,
          headers: { 'Content-Type': 'application/json' },
        },
      );
    });

    it.each([
      { testCase: 'HTTP 400', postResponse: { body: { message: 'Request failed with 400' }, status: 400 } },
      { testCase: 'HTTP 500', postResponse: { body: { message: 'Request failed with 500' }, status: 500 } },
      { testCase: 'network error', postResponse: { body: null, reject: true } },
    ])('should display generic error on create form when POST request is rejected with $testCase', async ({ postResponse }) => {
      // given
      const createTaskData = {
        title: 'Invalid Task',
        description: 'Some description',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };

      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: { body: [] },
          POST: postResponse,
        },
      });

      render(<TasksTable />);

      await user.click(screen.getByTestId('add-task-button'));
      await user.type(screen.getByTestId('create-task-title-input'), createTaskData.title);
      await user.type(screen.getByLabelText(/description/i), createTaskData.description);
      await user.click(screen.getByTestId('status-dropdown'));
      await user.click(screen.getByTestId(`status-dropdown-option-${createTaskData.status}`));
      await user.click(screen.getByTestId('priority-dropdown'));
      await user.click(screen.getByTestId(`priority-dropdown-option-${createTaskData.priority}`));

      // when
      await user.click(screen.getByTestId('create-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toHaveTextContent('Failed to create task. Please try again.');
      });
      expect(screen.getByTestId('create-task-title-input')).toBeVisible();
    });
  });
});
