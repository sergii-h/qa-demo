import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TaskPriority, TaskStatus } from '../../interfaces/ITask';
import { mockFetchResponse } from '../../test-utils/mockFetch';
import { TasksTable } from './tasksTable';

vi.stubGlobal('fetch', vi.fn());

describe('TasksTable integration', () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe('Basic flow', () => {
    it('should render tasks table with fetched tasks and action buttons', async () => {
      // given
      const tasks = [
        { id: '1', title: 'Task One', status: TaskStatus.TODO, priority: TaskPriority.LOW },
        { id: '2', title: 'Task Two', status: TaskStatus.IN_PROGRESS, priority: TaskPriority.MEDIUM },
        { id: '3', title: 'Task Three', status: TaskStatus.DONE, priority: TaskPriority.HIGH },
      ];

      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: { body: tasks },
        },
      });

      // when
      const { container } = render(<TasksTable />);

      // then
      await waitFor(() => {
        expect(screen.getByTestId('task-title-1')).toHaveTextContent('Task One');
        expect(screen.getByTestId('task-title-2')).toHaveTextContent('Task Two');
        expect(screen.getByTestId('task-title-3')).toHaveTextContent('Task Three');
      });

      expect(container.querySelectorAll('[data-testid^="task-title-"]')).toHaveLength(3);

      const firstRow = screen.getByTestId('task-title-1').closest('tr');
      expect(within(firstRow!).getByTestId(`status-tag-${TaskStatus.TODO}`)).toBeVisible();
      expect(within(firstRow!).getByTestId(`priority-tag-${TaskPriority.LOW}`)).toBeVisible();
      expect(within(firstRow!).getByTestId('info-button-1')).toBeVisible();
      expect(within(firstRow!).getByTestId('edit-button-1')).toBeVisible();
      expect(within(firstRow!).getByTestId('delete-button-1')).toBeVisible();

      const secondRow = screen.getByTestId('task-title-2').closest('tr');
      expect(within(secondRow!).getByTestId(`status-tag-${TaskStatus.IN_PROGRESS}`)).toBeVisible();
      expect(within(secondRow!).getByTestId(`priority-tag-${TaskPriority.MEDIUM}`)).toBeVisible();

      const thirdRow = screen.getByTestId('task-title-3').closest('tr');
      expect(within(thirdRow!).getByTestId(`status-tag-${TaskStatus.DONE}`)).toBeVisible();
      expect(within(thirdRow!).getByTestId(`priority-tag-${TaskPriority.HIGH}`)).toBeVisible();
    });

    it('should open create modal from create button', async () => {
      // given
      const task = {
        id: '10',
        title: 'Modal Task',
        description: 'Modal description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: true },
        },
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks`]: {
          GET: { body: [task] },
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId('add-task-button'));

      // then
      expect(screen.getByTestId('create-task-title-input')).toBeVisible();
    });

    it('should open info modal from info button', async () => {
      // given
      const task = {
        id: '10',
        title: 'Modal Task',
        description: 'Modal description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: true },
        },
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks`]: {
          GET: { body: [task] },
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toHaveTextContent(task.description);
      });
    });

    it('should open edit modal from edit button', async () => {
      // given
      const task = {
        id: '10',
        title: 'Modal Task',
        description: 'Modal description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: true },
        },
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks`]: {
          GET: { body: [task] },
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`task-title-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`edit-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toHaveValue(task.title);
      });
    });

    it('should render empty table when tasks api returns empty list', async () => {
      // given
      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: { body: [] },
        },
      });

      // when
      const { container } = render(<TasksTable />);

      // then
      await waitFor(() => {
        expect(globalThis.fetch).toHaveBeenCalledWith(
          expect.stringContaining('/v1/tasks'),
          expect.objectContaining({ method: 'GET' }),
        );
      });
      expect(screen.getByTestId('add-task-button')).toBeVisible();
      expect(container.querySelectorAll('[data-testid^="task-title-"]')).toHaveLength(0);
      expect(container.querySelectorAll('[data-testid^="info-button-"]')).toHaveLength(0);
      expect(container.querySelectorAll('[data-testid^="edit-button-"]')).toHaveLength(0);
      expect(container.querySelectorAll('[data-testid^="delete-button-"]')).toHaveLength(0);
    });
  });

  describe('Delete flow', () => {
    it('should delete task and remove row from table', async () => {
      // given
      const tasks = [
        { id: '1', title: 'Delete Me', status: TaskStatus.TODO, priority: TaskPriority.LOW },
        { id: '2', title: 'Keep Me', status: TaskStatus.DONE, priority: TaskPriority.HIGH },
      ];

      mockFetchResponse({
        [`/v1/tasks/1`]: {
          DELETE: { body: {}, status: 204 },
        },
        [`/v1/tasks`]: {
          GET: { body: tasks },
        },
      });

      const { container } = render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId('task-title-1')).toHaveTextContent('Delete Me');
      });

      // when
      await user.click(screen.getByTestId('delete-button-1'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('task-title-1')).not.toBeInTheDocument();
      });
      expect(globalThis.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/v1/tasks/1'),
        expect.objectContaining({ method: 'DELETE' }),
      );
      expect(screen.getByTestId('task-title-2')).toHaveTextContent('Keep Me');
      expect(container.querySelectorAll('[data-testid^="task-title-"]')).toHaveLength(1);
    });

    it.each([
      {
        testCase: 'api returns 500',
        deleteResponse: { body: { message: 'Delete failed' }, status: 500 },
      },
      {
        testCase: 'request fails due to network error',
        deleteResponse: { body: null, reject: true },
      },
    ])('should keep task row when delete $testCase', async ({ deleteResponse }) => {
      // given
      const tasks = [
        { id: '1', title: 'Delete Me', status: TaskStatus.TODO, priority: TaskPriority.LOW },
        { id: '2', title: 'Keep Me', status: TaskStatus.DONE, priority: TaskPriority.HIGH },
      ];

      mockFetchResponse({
        [`/v1/tasks/1`]: {
          DELETE: deleteResponse,
        },
        [`/v1/tasks`]: {
          GET: { body: tasks },
        },
      });

      const { container } = render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId('task-title-1')).toHaveTextContent('Delete Me');
      });

      // when
      await user.click(screen.getByTestId('delete-button-1'));

      // then
      await waitFor(() => {
        expect(globalThis.fetch).toHaveBeenCalledWith(
          expect.stringContaining('/v1/tasks/1'),
          expect.objectContaining({ method: 'DELETE' }),
        );
      });
      expect(screen.getByTestId('task-title-1')).toBeVisible();
      expect(screen.getByTestId('task-title-2')).toBeVisible();
      expect(container.querySelectorAll('[data-testid^="task-title-"]')).toHaveLength(2);
    });

    it('should delete task successfully after retry', async () => {
      // given
      const tasks = [
        { id: '1', title: 'Delete Me', status: TaskStatus.TODO, priority: TaskPriority.LOW },
        { id: '2', title: 'Keep Me', status: TaskStatus.DONE, priority: TaskPriority.HIGH },
      ];

      mockFetchResponse({
        [`/v1/tasks/1`]: {
          DELETE: [
            { body: { message: 'Delete failed' }, status: 500 },
            { body: {}, status: 204 },
          ],
        },
        [`/v1/tasks`]: {
          GET: { body: tasks },
        },
      });

      const { container } = render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId('task-title-1')).toHaveTextContent('Delete Me');
      });

      // when
      await user.click(screen.getByTestId('delete-button-1'));

      // then
      await waitFor(() => {
        expect(globalThis.fetch).toHaveBeenCalledWith(
          expect.stringContaining('/v1/tasks/1'),
          expect.objectContaining({ method: 'DELETE' }),
        );
      });
      expect(screen.getByTestId('task-title-1')).toBeVisible();
      expect(container.querySelectorAll('[data-testid^="task-title-"]')).toHaveLength(2);

      // when
      await user.click(screen.getByTestId('delete-button-1'));

      // then
      await waitFor(() => {
        expect(screen.queryByTestId('task-title-1')).not.toBeInTheDocument();
      });
      expect(screen.getByTestId('task-title-2')).toBeVisible();
      expect(container.querySelectorAll('[data-testid^="task-title-"]')).toHaveLength(1);
    });
  });

  describe('GET Error handling', () => {
    it('should keep create flow available when initial tasks request fails', async () => {
      // given
      mockFetchResponse({
        [`/v1/tasks`]: {
          GET: { body: { message: 'Server error' }, status: 500 },
        },
      });

      // when
      const { container } = render(<TasksTable />);

      // then
      await waitFor(() => {
        expect(screen.getByTestId('add-task-button')).toBeVisible();
      });
      expect(container.querySelectorAll('[data-testid^="task-title-"]')).toHaveLength(0);
    });
  });
});
