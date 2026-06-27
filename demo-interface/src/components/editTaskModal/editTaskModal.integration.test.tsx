import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TaskPriority, TaskStatus } from '../../interfaces/ITask';
import { TasksTable } from '../tasksTable/tasksTable';
import { mockFetchResponse } from '../../test-utils/mockFetch';

vi.stubGlobal('fetch', vi.fn());

describe('EditTaskModal integration', () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe('Edit task tests', () => {
    it('should update task with modified values, send correct PUT request and show modified task title in the list after successful response', async () => {
      // given
      const existingTask = {
        id: 'task-201',
        title: 'Existing title',
        description: 'Existing description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      const updatedTask = {
        ...existingTask,
        title: 'Updated title',
        description: 'Updated description',
        status: TaskStatus.DONE,
        priority: TaskPriority.HIGH,
      };

      mockFetchResponse({
        [`/v1/tasks/${existingTask.id}`]: {
          GET: { body: existingTask },
          PUT: { body: updatedTask, status: 200 },
        },
        [`/v1/tasks`]: {
          GET: [{ body: [existingTask] }, { body: [updatedTask] }],
        },
      });

      render(<TasksTable />);

      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${existingTask.id}`)).toHaveTextContent(existingTask.title);
      });

      await user.click(screen.getByTestId(`edit-button-${existingTask.id}`));

      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toHaveValue(existingTask.title);
      });

      await user.clear(screen.getByTestId('edit-task-title-input'));
      await user.type(screen.getByTestId('edit-task-title-input'), updatedTask.title);
      await user.clear(screen.getByLabelText(/description/i));
      await user.type(screen.getByLabelText(/description/i), updatedTask.description);
      await user.click(screen.getByTestId('status-dropdown'));
      await user.click(await screen.findByRole('option', { name: 'Done', hidden: true }));
      await user.click(screen.getByTestId('priority-dropdown'));
      await user.click(await screen.findByRole('option', { name: 'High', hidden: true }));

      // when
      await user.click(screen.getByTestId('save-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('edit-task-title-input')).not.toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${existingTask.id}`)).toHaveTextContent(updatedTask.title);
      });
    });

    it('should update task with removed description', async () => {
      // given
      const existingTask = {
        id: 'task-210',
        title: 'Task with description',
        description: 'Description to remove',
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.MEDIUM,
      };
      const updatedTask = {
        ...existingTask,
        description: '',
      };

      mockFetchResponse({
        [`/v1/tasks/${existingTask.id}`]: {
          GET: { body: existingTask },
          PUT: { body: updatedTask, status: 200 },
        },
        [`/v1/tasks`]: {
          GET: [{ body: [existingTask] }, { body: [updatedTask] }],
        },
      });

      render(<TasksTable />);

      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${existingTask.id}`)).toHaveTextContent(existingTask.title);
      });

      await user.click(screen.getByTestId(`edit-button-${existingTask.id}`));
      await waitFor(() => {
        expect(screen.getByDisplayValue(existingTask.description)).toBeVisible();
      });

      await user.clear(screen.getByDisplayValue(existingTask.description));

      // when
      await user.click(screen.getByTestId('save-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('edit-task-title-input')).not.toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${existingTask.id}`)).toHaveTextContent(existingTask.title);
      });

      expect(globalThis.fetch).toHaveBeenNthCalledWith(3,
        expect.stringContaining(`/v1/tasks/${existingTask.id}`),
        {
          method: 'PUT',
          body: JSON.stringify({
            title: existingTask.title,
            description: '',
            status: existingTask.status,
            priority: existingTask.priority,
          }),
          headers: { 'Content-Type': 'application/json' },
        },
      );
    });

    it('should not modify task when edit form is closed without saving', async () => {
      // given
      const task = {
        id: 'task-202',
        title: 'Original title',
        description: 'Original description',
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.LOW,
      };

      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks`]: {
          GET: { body: [task] },
        },
      });

      render(<TasksTable />);

      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${task.id}`)).toHaveTextContent(task.title);
      });

      await user.click(screen.getByTestId(`edit-button-${task.id}`));
      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });

      await user.clear(screen.getByTestId('edit-task-title-input'));
      await user.type(screen.getByTestId('edit-task-title-input'), 'Unsaved title');

      // when
      await user.click(screen.getByTestId('close-button'));

      // then
      expect(screen.queryByTestId('edit-task-title-input')).not.toBeInTheDocument();
      expect(screen.getByTestId(`task-title-${task.id}`)).toHaveTextContent(task.title);
      expect(vi.mocked(globalThis.fetch).mock.calls.filter(([, payload]) =>
        (payload as { method?: string } | undefined)?.method === 'PUT',
      )).toHaveLength(0);
    });

    it('should proceed with save after user corrects invalid title', async () => {
      // given
      const task = {
        id: 'task-203',
        title: 'Recover task',
        description: 'Recover description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      const correctedTask = {
        ...task,
        title: 'Recovered title',
      };

      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
          PUT: { body: correctedTask, status: 200 },
        },
        [`/v1/tasks`]: {
          GET: [{ body: [task] }, { body: [correctedTask] }],
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`edit-button-${task.id}`)).toBeVisible();
      });

      await user.click(screen.getByTestId(`edit-button-${task.id}`));
      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });

      await user.clear(screen.getByTestId('edit-task-title-input'));
      await user.type(screen.getByTestId('edit-task-title-input'), 'a'.repeat(101));

      // when
      await user.click(screen.getByTestId('save-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toBeVisible();
      });
      expect(vi.mocked(globalThis.fetch).mock.calls.filter(([, payload]) =>
        (payload as { method?: string } | undefined)?.method === 'PUT',
      )).toHaveLength(0);

      // when
      await user.clear(screen.getByTestId('edit-task-title-input'));
      await user.type(screen.getByTestId('edit-task-title-input'), correctedTask.title);
      await user.click(screen.getByTestId('save-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('edit-task-title-input')).not.toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${task.id}`)).toHaveTextContent(correctedTask.title);
      });
    });

    it.each([
      { testCase: 'HTTP 500', getTaskResponse: { body: { message: 'Request failed with 500' }, status: 500 } },
      { testCase: 'network error', getTaskResponse: { body: null, reject: true } },
    ])('should allow opening edit form when initial GET tasks fails with $testCase', async ({ getTaskResponse }) => {
      // given
      const task = {
        id: 'task-207',
        title: 'Get error task',
        description: 'Get error description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };

      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: getTaskResponse,
        },
        [`/v1/tasks`]: {
          GET: { body: [task] },
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`edit-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`edit-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });
      expect(screen.getByTestId('save-button')).toBeDisabled();
    });

    it.each([
      { testCase: 'HTTP 500', refreshGetResponse: { body: null, status: 500 } },
      { testCase: 'network error', refreshGetResponse: { body: null, reject: 'Refresh failed' } },
    ])('should close edit form when refresh GET fails with $testCase after successful PUT', async ({ refreshGetResponse }) => {
      // given
      const existingTask = {
        id: 'task-211',
        title: 'Task with refresh failure',
        description: 'Refresh failure description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      const updatedTask = {
        ...existingTask,
        title: 'Updated after refresh failure',
      };

      mockFetchResponse({
        [`/v1/tasks/${existingTask.id}`]: {
          GET: { body: existingTask },
          PUT: { body: updatedTask, status: 200 },
        },
        [`/v1/tasks`]: {
          GET: [
            { body: [existingTask] },
            refreshGetResponse,
          ],
        },
      });

      render(<TasksTable />);

      await waitFor(() => {
        expect(screen.getByTestId(`edit-button-${existingTask.id}`)).toBeVisible();
      });

      await user.click(screen.getByTestId(`edit-button-${existingTask.id}`));
      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toHaveValue(existingTask.title);
      });
      await user.clear(screen.getByTestId('edit-task-title-input'));
      await user.type(screen.getByTestId('edit-task-title-input'), updatedTask.title);

      // when
      await user.click(screen.getByTestId('save-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('edit-task-title-input')).not.toBeInTheDocument();
      });
      expect(globalThis.fetch).toHaveBeenNthCalledWith(4,
        expect.stringContaining('/v1/tasks'),
        {
          method: 'GET',
          body: undefined,
          headers: { 'Content-Type': 'application/json' },
        },
      );
    });

    it('should allow retry and save task after initial PUT failure', async () => {
      // given
      const task = {
        id: 'task-205',
        title: 'Retry edit',
        description: 'Retry description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      const updatedTask = {
        ...task,
        title: 'Retry edit updated',
      };

      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
          PUT: [
            { body: { message: 'Temporary failure' }, status: 500 },
            { body: updatedTask, status: 200 },
          ],
        },
        [`/v1/tasks`]: {
          GET: [{ body: [task] }, { body: [updatedTask] }],
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`edit-button-${task.id}`)).toBeVisible();
      });

      await user.click(screen.getByTestId(`edit-button-${task.id}`));
      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });
      await user.clear(screen.getByTestId('edit-task-title-input'));
      await user.type(screen.getByTestId('edit-task-title-input'), updatedTask.title);

      // when
      await user.click(screen.getByTestId('save-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId('save-button'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('edit-task-title-input')).not.toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${task.id}`)).toHaveTextContent(updatedTask.title);
      });
    });

    it.each([
      { testCase: 'HTTP 400', putResponse: { body: { message: 'Request failed with 400' }, status: 400 } },
      { testCase: 'HTTP 500', putResponse: { body: { message: 'Request failed with 500' }, status: 500 } },
      { testCase: 'network error', putResponse: { body: null, reject: true } },
    ])('should display generic error on edit form when PUT request is rejected with $testCase', async ({ putResponse }) => {
      // given
      const task = {
        id: 'task-204',
        title: 'Error task',
        description: 'Error description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };

      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
          PUT: putResponse,
        },
        [`/v1/tasks`]: {
          GET: { body: [task] },
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`edit-button-${task.id}`)).toBeVisible();
      });

      await user.click(screen.getByTestId(`edit-button-${task.id}`));
      await waitFor(() => {
        expect(screen.getByTestId('save-button')).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId('save-button'));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toHaveTextContent('Failed to update task. Please try again.');
      });
      expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
    });
  });
});
