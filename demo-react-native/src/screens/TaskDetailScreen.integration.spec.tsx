import {fireEvent, waitFor} from '@testing-library/react-native';

import {TaskDetailScreen} from './TaskDetailScreen';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {mockFetchResponse} from '@/test-utils/mockFetch';
import {TestTags} from '@/testTags';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

const navigation = {
  goBack: jest.fn(),
};

function detailRoute(taskId: string) {
  return {
    key: 'TaskDetail',
    name: 'TaskDetail' as const,
    params: { taskId },
  };
}

describe('TaskDetailScreen integration', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    global.fetch = jest.fn();
  });

  describe('Info task tests', () => {
    it('should open info form and display task details for all values dataset', async () => {
      // Given
      const task = {
        id: 'task-301',
        title: 'Info task',
        description: 'Info description',
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: true },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskDetailScreen navigation={navigation as never} route={detailRoute(task.id) as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.DESCRIPTION)).toHaveTextContent('Info description');
        expect(screen.getByTestId(TestTags.statusTag(TaskStatus.IN_PROGRESS))).toBeVisible();
        expect(screen.getByTestId(TestTags.priorityTag(TaskPriority.HIGH))).toBeVisible();
        expect(screen.getByTestId(TestTags.VALID)).toBeVisible();
      });
    });

    it('should open info form and display task details for required only values dataset', async () => {
      // Given
      const task = {
        id: 'task-305',
        title: 'Info required task',
        description: null,
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: true },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskDetailScreen navigation={navigation as never} route={detailRoute(task.id) as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.DESCRIPTION)).toHaveTextContent('No description');
        expect(screen.getByTestId(TestTags.statusTag(TaskStatus.TODO))).toBeVisible();
        expect(screen.getByTestId(TestTags.priorityTag(TaskPriority.MEDIUM))).toBeVisible();
        expect(screen.getByTestId(TestTags.VALID)).toBeVisible();
      });
    });

    it('should close info form on close action', async () => {
      // Given
      const task = {
        id: 'task-1',
        title: 'Info Task',
        description: 'Info description',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: true },
        },
      });
      const screen = renderWithProviders(
        <TaskDetailScreen navigation={navigation as never} route={detailRoute(task.id) as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.DESCRIPTION)).toBeVisible();
      });

      // When
      fireEvent.press(screen.getByTestId(TestTags.CLOSE_BUTTON));

      // Then
      expect(navigation.goBack).toHaveBeenCalled();
    });
  });

  describe('UI-006', () => {
    it('should display validated state when external validation returns true', async () => {
      // Given
      const task = {
        id: 'task-306-valid',
        title: 'Valid info task',
        description: 'Valid description',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };
      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: true },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskDetailScreen navigation={navigation as never} route={detailRoute(task.id) as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.VALID)).toBeVisible();
      });
      expect(screen.queryByTestId(TestTags.NOT_VALID)).toBeNull();
    });

    it('should display not-validated state when external validation returns false', async () => {
      // Given
      const task = {
        id: 'task-306',
        title: 'Not valid info task',
        description: 'Not valid description',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };
      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: { body: false },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskDetailScreen navigation={navigation as never} route={detailRoute(task.id) as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.DESCRIPTION)).toHaveTextContent(task.description);
        expect(screen.getByTestId(TestTags.NOT_VALID)).toBeVisible();
      });
      expect(screen.queryByTestId(TestTags.VALID)).toBeNull();
    });

    it.each([
      { testCase: 'HTTP 500', taskGetResponse: { status: 500, ok: false, body: { message: 'Request failed with 500' } } },
      { testCase: 'network error', taskGetResponse: { reject: true } },
    ])('should not open info form when task details request fails with $testCase and display generic load task info error', async ({ taskGetResponse }) => {
      // Given
      mockFetchResponse({
        '/v1/tasks/task-303': {
          GET: taskGetResponse,
        },
        '/v1/tasks/isValid/task-303': {
          GET: { body: false },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskDetailScreen navigation={navigation as never} route={detailRoute('task-303') as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.LOAD_ERROR)).toBeVisible();
      });
      expect(screen.queryByTestId(TestTags.DESCRIPTION)).toBeNull();
    });

    it.each([
      { testCase: 'HTTP 500', validationGetResponse: { status: 500, ok: false, body: { message: 'Validation failed' } } },
      { testCase: 'network error', validationGetResponse: { reject: true } },
    ])('should show invalid validation sign when validation request fails with $testCase and display generic load task info error', async ({ validationGetResponse }) => {
      // Given
      const task = {
        id: 'task-307',
        title: 'Validation 500 task',
        description: 'Validation 500 description',
        status: TaskStatus.DONE,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
        [`/v1/tasks/isValid/${task.id}`]: {
          GET: validationGetResponse,
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskDetailScreen navigation={navigation as never} route={detailRoute(task.id) as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.DESCRIPTION)).toHaveTextContent(task.description);
        expect(screen.getByTestId(TestTags.NOT_VALID)).toBeVisible();
      });
      expect(screen.queryByTestId(TestTags.LOAD_ERROR)).toBeNull();
    });
  });
});
