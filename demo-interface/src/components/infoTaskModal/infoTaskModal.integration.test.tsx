import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TaskPriority, TaskStatus } from '../../interfaces/ITask';
import { TasksTable } from '../tasksTable/tasksTable';
import { mockFetchResponse } from '../../test-utils/mockFetch';

vi.stubGlobal('fetch', vi.fn());

describe('InfoTaskModal integration', () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    user = userEvent.setup();
  });

  describe('Basic flow', () => {
    it.each([
      {
        testCase: 'all values',
        task: {
          id: 'task-301',
          title: 'Info task',
          description: 'Info description',
          status: TaskStatus.IN_PROGRESS,
          priority: TaskPriority.HIGH,
          createdDate: '2024-01-15T10:00:00.000Z',
          updatedDate: '2024-01-16T12:00:00.000Z',
        },
        expectedDescription: 'Info description',
      },
      {
        testCase: 'required values',
        task: {
          id: 'task-305',
          title: 'Info required task',
          description: '',
          status: TaskStatus.TODO,
          priority: TaskPriority.MEDIUM,
        },
        expectedDescription: 'No description',
      },
    ])('should open info modal and display task details with $testCase', async ({ task, expectedDescription }) => {
      // given
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
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toHaveTextContent(expectedDescription);
      });
      const infoDialog = screen.getByTestId('description').closest('.p-dialog') as HTMLElement;
      expect(within(infoDialog).getByTestId(`status-tag-${task.status}`)).toBeVisible();
      expect(within(infoDialog).getByTestId(`priority-tag-${task.priority}`)).toBeVisible();
      expect(within(infoDialog).getByTestId('valid')).toBeVisible();
    });

    it('should show not-valid indicator when validation result is false', async () => {
      // given
      const task = {
        id: 'task-306',
        title: 'Not valid info task',
        description: 'Not valid description',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: false },
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
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toHaveTextContent(task.description);
      });
      const infoDialog = screen.getByTestId('description').closest('.p-dialog') as HTMLElement;
      expect(within(infoDialog).getByTestId('notValid')).toBeVisible();
    });
  });

  describe('Close form', () => {
    it('should close info modal when close button is clicked', async () => {
      // given
      const task = {
        id: 'task-302',
        title: 'Closable info task',
        description: 'Closable description',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: false },
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
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });
      await user.click(screen.getByTestId(`info-button-${task.id}`));
      await waitFor(() => {
        expect(screen.getByTestId('modal-title')).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId('close-button'));

      // then
      expect(screen.queryByTestId('description')).not.toBeInTheDocument();
    });
  });

  describe('GET Error handling', () => {
    it('should keep info flow available when task details request returns 500', async () => {
      // given
      const task = {
        id: 'task-303',
        title: 'Fallback info task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: false },
        },
        [`/v1/tasks/${task.id}`]: {
          GET: {
            body: {
              ...task,
              description: undefined,
            },
            status: 500,
          },
        },
        [`/v1/tasks`]: {
          GET: { body: [task] },
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toBeVisible();
      });
      expect(screen.getByTestId('notValid')).toBeVisible();
    });

    it('should keep info flow available when validation request returns 500', async () => {
      // given
      const task = {
        id: 'task-307',
        title: 'Validation 500 task',
        description: 'Validation 500 description',
        status: TaskStatus.DONE,
        priority: TaskPriority.MEDIUM,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: false, status: 500 },
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
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toHaveTextContent(task.description);
      });
      expect(screen.getByTestId('notValid')).toBeVisible();
    });
  });

  describe('Network failure handling', () => {
    it('should keep info flow available when task details request is rejected', async () => {
      // given
      const task = {
        id: 'task-308',
        title: 'Task reject task',
        description: 'Task reject description',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: false },
        },
        [`/v1/tasks/${task.id}`]: {
          GET: { body: null, reject: true },
        },
        [`/v1/tasks`]: {
          GET: { body: [task] },
        },
      });

      render(<TasksTable />);
      await waitFor(() => {
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toHaveTextContent('No description');
      });
      expect(screen.getByTestId('notValid')).toBeVisible();
    });

    it('should keep info flow available when validation request is rejected', async () => {
      // given
      const task = {
        id: 'task-304',
        title: 'Validation reject task',
        description: 'Validation reject description',
        status: TaskStatus.DONE,
        priority: TaskPriority.HIGH,
      };

      mockFetchResponse({
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: null, reject: true },
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
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toHaveTextContent(task.description);
      });
      expect(screen.getByTestId('notValid')).toBeVisible();
    });
  });
});
