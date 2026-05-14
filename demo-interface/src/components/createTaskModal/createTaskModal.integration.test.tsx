import { fireEvent, render, screen, waitFor } from '@testing-library/react';
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

  describe('Basic flow', () => {
    it.each([
      {
        testCase: 'all values',
        createTaskData: {
          id: 'task-123',
          title: 'Test Task',
          description: 'Test Description',
          status: TaskStatus.TODO,
          priority: TaskPriority.HIGH,
        },
      },
      {
        testCase: 'required values',
        createTaskData: {
          id: 'task-124',
          title: 'Test Task',
          description: '',
          status: TaskStatus.TODO,
          priority: TaskPriority.HIGH,
        },
      },
    ])('should create task with $testCase', async ({ createTaskData }) => {
      // given
      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: [
            { body: [] },
            { body: [createTaskData] }
          ],
          POST: {
            body: {
              ...createTaskData,
            }
          }
        },
      });

      render(<TasksTable />);

      await user.click(screen.getByTestId('add-task-button'));

      await user.type(screen.getByTestId('create-task-title-input'), createTaskData.title);
      fireEvent.change(screen.getByLabelText(/description/i), { target: { value: createTaskData.description } });
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
        expect.stringContaining(`/v1/tasks`),
        {
          method: 'GET',
          body: undefined,
          headers: { 'Content-Type': 'application/json' }
        }
      );

      expect(globalThis.fetch).toHaveBeenNthCalledWith(2,
        expect.stringContaining(`/v1/tasks`),
        {
          method: 'POST',
          body: JSON.stringify({
            title: createTaskData.title,
            description: createTaskData.description,
            status: createTaskData.status,
            priority: createTaskData.priority,
          }),
          headers: { 'Content-Type': 'application/json' }
        }
      );

      expect(globalThis.fetch).toHaveBeenNthCalledWith(3,
        expect.stringContaining(`/v1/tasks`),
        {
          method: 'GET',
          body: undefined,
          headers: { 'Content-Type': 'application/json' }
        }
      );
    });
  });

  describe('Close form', () => {
    it('should reset form state when modal is closed and opened again', async () => {
      // given
      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: { body: [] }
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
  });

  describe('Form validation', () => {
    it('should proceed with creation after user corrects invalid title', async () => {
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
            { body: [createTaskData] }
          ],
          POST: {
            body: createTaskData,
            status: 201,
          }
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
  });

  describe('Error handling', () => {
    describe('POST Error handling', () => {
      it.each([400, 500])('should display generic error when api returns %i', async (statusCode) => {
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
            POST: {
              body: { message: `Request failed with ${statusCode}` },
              status: statusCode,
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
          expect(screen.getByTestId('title-error')).toHaveTextContent('Failed to create task. Please try again.');
        });
        expect(screen.getByTestId('create-task-title-input')).toBeVisible();
      });
  
      it('should allow retry and create task after initial post failure', async () => {
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
              { body: [createTaskData] }
            ],
            POST: [
              {
                body: null,
                status: 500,
              },
              {
                body: createTaskData,
                status: 201,
              }
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
    });
  
    describe('GET Error handling', () => {
      it('should keep create flow available when initial get request returns 500', async () => {
        // given
        mockFetchResponse({
          [`/v1/tasks`]: {
            GET: {
              body: { message: 'Server error' },
              status: 500,
            }
          },
        });
  
        render(<TasksTable />);
  
        // then
        await waitFor(() => {
          expect(screen.getByTestId('add-task-button')).toBeVisible();
        });
        expect(globalThis.fetch).toHaveBeenCalledTimes(1);
      });
  
      it('should close modal even when refresh get request fails after successful post', async () => {
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
              { body: null, status: 500 }
            ],
            POST: {
              body: {
                id: 'task-778',
                ...createTaskData,
              },
              status: 201
            }
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
          expect.stringContaining(`/v1/tasks`),
          {
            method: 'GET',
            body: undefined,
            headers: { 'Content-Type': 'application/json' }
          }
        );
      });
    });
  
    describe('Network failure handling', () => {
      it('should display error message when post request is rejected', async () => {
        // given
        mockFetchResponse({
          [`/v1/tasks`]: {
            GET: { body: [] },
            POST: {
              body: null,
              reject: true,
            },
          },
        });
  
        render(<TasksTable />);
  
        await user.click(screen.getByTestId('add-task-button'));
        await user.type(screen.getByTestId('create-task-title-input'), 'Test Task');
        await user.click(screen.getByTestId('status-dropdown'));
        await user.click(screen.getByTestId(`status-dropdown-option-${TaskStatus.TODO}`));
        await user.click(screen.getByTestId('priority-dropdown'));
        await user.click(screen.getByTestId(`priority-dropdown-option-${TaskPriority.HIGH}`));
  
        // when
        await user.click(screen.getByTestId('create-button'));
  
        // then
        await waitFor(() => {
          expect(screen.getByTestId('title-error')).toBeVisible();
        });
        expect(screen.getByTestId('create-task-title-input')).toBeVisible();
      });
  
      it('should keep create flow available when initial get request is rejected', async () => {
        // given
        mockFetchResponse({
          [`/v1/tasks`]: {
            GET: {
              body: null,
              reject: 'Initial load failed',
            },
          },
        });
  
        render(<TasksTable />);
  
        // then
        await waitFor(() => {
          expect(screen.getByTestId('add-task-button')).toBeVisible();
        });
      });
  
      it('should close modal when refresh get request is rejected after successful post', async () => {
        // given
        const createdTask = {
          id: 'task-779',
          title: 'Task with network refresh failure',
          description: '',
          status: TaskStatus.TODO,
          priority: TaskPriority.HIGH,
        };
  
        mockFetchResponse({
          [`/v1/tasks`]: {
            GET: [
              { body: [] },
              { body: null, reject: 'Refresh failed' }
            ],
            POST: {
              body: createdTask,
              status: 201
            }
          },
        });
  
        render(<TasksTable />);
  
        await user.click(screen.getByTestId('add-task-button'));
        await user.type(screen.getByTestId('create-task-title-input'), createdTask.title);
        await user.click(screen.getByTestId('status-dropdown'));
        await user.click(screen.getByTestId(`status-dropdown-option-${createdTask.status}`));
        await user.click(screen.getByTestId('priority-dropdown'));
        await user.click(screen.getByTestId(`priority-dropdown-option-${createdTask.priority}`));
  
        // when
        await user.click(screen.getByTestId('create-button'));
  
        // then
        await waitFor(() => {
          expect(screen.queryByTestId('create-task-title-input')).not.toBeInTheDocument();
        });
        expect(globalThis.fetch).toHaveBeenCalledTimes(3);
      });
    });
  });
});

