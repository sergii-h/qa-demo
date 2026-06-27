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

  describe('Info task tests', () => {
    it('should open info form and display task details for all values dataset', async () => {
      // given
      const task = {
        id: 'task-301',
        title: 'Info task',
        description: 'Info description',
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.HIGH,
        createdDate: '2024-01-15T10:00:00.000Z',
        updatedDate: '2024-01-16T12:00:00.000Z',
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
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toHaveTextContent(task.description);
      });
      const infoDialog = screen.getByTestId('description').closest('.p-dialog') as HTMLElement;
      expect(within(infoDialog).getByTestId(`status-tag-${task.status}`)).toBeVisible();
      expect(within(infoDialog).getByTestId(`priority-tag-${task.priority}`)).toBeVisible();
      expect(within(infoDialog).getByTestId('valid')).toBeVisible();
    });

    it('should open info form and display task details for required only values dataset', async () => {
      // given
      const task = {
        id: 'task-305',
        title: 'Info required task',
        description: '',
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
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('description')).toHaveTextContent('No description');
      });
      const infoDialog = screen.getByTestId('description').closest('.p-dialog') as HTMLElement;
      expect(within(infoDialog).getByTestId(`status-tag-${task.status}`)).toBeVisible();
      expect(within(infoDialog).getByTestId(`priority-tag-${task.priority}`)).toBeVisible();
      expect(within(infoDialog).getByTestId('valid')).toBeVisible();
    });

    it('should close info form on close action', async () => {
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

  describe('UI-006', () => {
    it('should display validated state when external validation returns true', async () => {
      // given
      const task = {
        id: 'task-306-valid',
        title: 'Valid info task',
        description: 'Valid description',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
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
        expect(screen.getByTestId(`info-button-${task.id}`)).toBeVisible();
      });

      // when
      await user.click(screen.getByTestId(`info-button-${task.id}`));

      // then
      await waitFor(() => {
        expect(screen.getByTestId('valid')).toBeVisible();
      });
      expect(screen.queryByTestId('notValid')).not.toBeInTheDocument();
    });

    it('should display not-validated state when external validation returns false', async () => {
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
      expect(within(infoDialog).queryByTestId('valid')).not.toBeInTheDocument();
    });

    it.each([
      { testCase: 'HTTP 500', taskGetResponse: { body: { message: 'Request failed with 500' }, status: 500 } },
      { testCase: 'network error', taskGetResponse: { body: null, reject: true } },
    ])('should not open info form when task details request fails with $testCase and display generic load task info error', async ({ taskGetResponse }) => {
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
          GET: taskGetResponse,
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
        expect(screen.getByTestId('load-error')).toHaveTextContent('Failed to load task info. Please try again.');
      });
      expect(screen.queryByTestId('description')).not.toBeInTheDocument();
    });

    it.each([
      { testCase: 'HTTP 500', validationGetResponse: { body: { message: 'Validation failed' }, status: 500 } },
      { testCase: 'network error', validationGetResponse: { body: null, reject: true } },
    ])('should show invalid validation sign when validation request fails with $testCase and display generic load task info error', async ({ validationGetResponse }) => {
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
          GET: validationGetResponse,
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
      expect(screen.queryByTestId('load-error')).not.toBeInTheDocument();
    });
  });
});
