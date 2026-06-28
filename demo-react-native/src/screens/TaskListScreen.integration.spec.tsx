import {act, fireEvent, RenderAPI, waitFor} from '@testing-library/react-native';

import {ApiError} from '@/data/remote/apiClient';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {taskRepository} from '@/repository/taskRepository';
import {TaskListScreen} from './TaskListScreen';
import {mockFetchResponse} from '@/test-utils/mockFetch';
import {TestTags} from '@/testTags';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

async function pullToRefresh(screen: RenderAPI) {
  const list = screen.getByTestId(TestTags.TASK_LIST);
  const onRefresh = list.props.refreshControl?.props?.onRefresh;

  if (onRefresh) {
    await act(async () => {
      await onRefresh();
    });
    return;
  }

  fireEvent(list, 'refresh');
}

function isDeleteButtonEnabled(screen: RenderAPI, taskId: string) {
  const deleteButton = screen.getByTestId(TestTags.deleteButton(taskId));
  return (
    deleteButton.props.disabled !== true &&
    deleteButton.props.accessibilityState?.disabled !== true
  );
}

async function pressDeleteWhenEnabled(screen: RenderAPI, taskId: string) {
  await waitFor(() => {
    expect(isDeleteButtonEnabled(screen, taskId)).toBe(true);
  });
  fireEvent.press(screen.getByTestId(TestTags.deleteButton(taskId)));
}

async function retryDeleteUntilTaskRemoved(screen: RenderAPI, taskId: string) {
  await waitFor(
    () => {
      if (screen.queryByTestId(TestTags.taskTitle(taskId))) {
        if (isDeleteButtonEnabled(screen, taskId)) {
          fireEvent.press(screen.getByTestId(TestTags.deleteButton(taskId)));
        }
      }
      expect(screen.queryByTestId(TestTags.taskTitle(taskId))).toBeNull();
    },
    { timeout: 5000 },
  );
}

const navigation = {
  navigate: jest.fn(),
};

const route = {
  key: 'TaskList',
  name: 'TaskList' as const,
  params: undefined,
};

const tagVariantTasks = [
  { id: '1', title: 'Task One', status: TaskStatus.TODO, priority: TaskPriority.LOW },
  { id: '2', title: 'Task Two', status: TaskStatus.IN_PROGRESS, priority: TaskPriority.MEDIUM },
  { id: '3', title: 'Task Three', status: TaskStatus.DONE, priority: TaskPriority.HIGH },
];

describe('TaskListScreen integration', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    global.fetch = jest.fn();
  });

  describe('Task table tests', () => {
    it('should render task list with fetched data when the list is first shown', async () => {
      // Given
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: tagVariantTasks.slice(0, 2) },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle('1'))).toBeVisible();
        expect(screen.getByTestId(TestTags.taskTitle('2'))).toBeVisible();
      });
    });

    it('should display status/priority tags and action buttons for each task', async () => {
      // Given
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: tagVariantTasks },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle('1'))).toBeVisible();
        expect(screen.getByTestId(TestTags.taskTitle('2'))).toBeVisible();
        expect(screen.getByTestId(TestTags.taskTitle('3'))).toBeVisible();
      });
      expect(screen.getByTestId(TestTags.statusTag(TaskStatus.TODO))).toBeVisible();
      expect(screen.getByTestId(TestTags.priorityTag(TaskPriority.LOW))).toBeVisible();
      expect(screen.getByTestId(TestTags.statusTag(TaskStatus.IN_PROGRESS))).toBeVisible();
      expect(screen.getByTestId(TestTags.priorityTag(TaskPriority.MEDIUM))).toBeVisible();
      expect(screen.getByTestId(TestTags.statusTag(TaskStatus.DONE))).toBeVisible();
      expect(screen.getByTestId(TestTags.priorityTag(TaskPriority.HIGH))).toBeVisible();
      expect(screen.getByTestId(TestTags.infoButton('1'))).toBeVisible();
      expect(screen.getByTestId(TestTags.editButton('1'))).toBeVisible();
      expect(screen.getByTestId(TestTags.deleteButton('1'))).toBeVisible();
      expect(screen.getByTestId(TestTags.infoButton('2'))).toBeVisible();
      expect(screen.getByTestId(TestTags.editButton('2'))).toBeVisible();
      expect(screen.getByTestId(TestTags.deleteButton('2'))).toBeVisible();
      expect(screen.getByTestId(TestTags.infoButton('3'))).toBeVisible();
      expect(screen.getByTestId(TestTags.editButton('3'))).toBeVisible();
      expect(screen.getByTestId(TestTags.deleteButton('3'))).toBeVisible();
    });

    it('should render empty list state when tasks response is empty', async () => {
      // Given
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: [] },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.EMPTY_TASKS)).toBeVisible();
        expect(screen.getByTestId(TestTags.ADD_TASK_BUTTON)).toBeVisible();
      });
    });

    it('should open create task form from list actions', async () => {
      // Given
      const task = {
        id: '10',
        title: 'Modal Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: [task] },
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(task.id))).toBeVisible();
      });

      // When
      fireEvent.press(screen.getByTestId(TestTags.ADD_TASK_BUTTON));

      // Then
      expect(navigation.navigate).toHaveBeenCalledWith('CreateTask');
    });

    it('should open task info form from list actions', async () => {
      // Given
      const task = {
        id: '10',
        title: 'Modal Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: [task] },
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(task.id))).toBeVisible();
      });

      // When
      fireEvent.press(screen.getByTestId(TestTags.infoButton(task.id)));

      // Then
      expect(navigation.navigate).toHaveBeenCalledWith('TaskDetail', { taskId: task.id });
    });

    it('should open task edit form from list actions', async () => {
      // Given
      const task = {
        id: '10',
        title: 'Modal Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: [task] },
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(task.id))).toBeVisible();
      });

      // When
      fireEvent.press(screen.getByTestId(TestTags.editButton(task.id)));

      // Then
      expect(navigation.navigate).toHaveBeenCalledWith('EditTask', { taskId: task.id });
    });

    it('should keep create flow available when initial GET tasks request fails', async () => {
      // Given
      mockFetchResponse({
        '/v1/tasks': {
          GET: { status: 500, ok: false, body: { message: 'Server error' } },
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.ADD_TASK_BUTTON)).toBeVisible();
      });
      expect(screen.queryByTestId(TestTags.taskTitle('1'))).toBeNull();
    });

  });

  describe('Delete task tests', () => {
    it('should send delete request with selected task ID when delete is triggered and remove task from list after successful delete response', async () => {
      // Given
      const deleteTask = {
        id: '1',
        title: 'Delete Me',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };
      const keepTask = {
        id: '2',
        title: 'Keep Me',
        status: TaskStatus.DONE,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: [deleteTask, keepTask] },
        },
        [`/v1/tasks/${deleteTask.id}`]: {
          DELETE: { status: 204, body: null },
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(deleteTask.id))).toBeVisible();
      });

      // When
      fireEvent.press(screen.getByTestId(TestTags.deleteButton(deleteTask.id)));

      // Then
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          expect.stringContaining(`/v1/tasks/${deleteTask.id}`),
          expect.objectContaining({ method: 'DELETE' }),
        );
      });
      await waitFor(() => {
        expect(screen.queryByTestId(TestTags.taskTitle(deleteTask.id))).toBeNull();
      });
      expect(screen.getByTestId(TestTags.taskTitle(keepTask.id))).toBeVisible();
      expect(screen.getByTestId(TestTags.TASK_LIST)).toBeVisible();
    });

    it.each([
      {
        testCase: 'HTTP 500',
        deleteResponse: { status: 500, ok: false, body: { message: 'Delete failed' } },
        expectedError: 'Delete failed',
      },
      { testCase: 'network error', deleteResponse: { reject: true }, expectedError: 'Network error' },
    ])('should keep task in list when delete fails with $testCase', async ({ deleteResponse, expectedError }) => {
      // Given
      const deleteTask = {
        id: '1',
        title: 'Delete Me',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };
      const keepTask = {
        id: '2',
        title: 'Keep Me',
        status: TaskStatus.DONE,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: [deleteTask, keepTask] },
        },
        [`/v1/tasks/${deleteTask.id}`]: {
          DELETE: deleteResponse,
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(deleteTask.id))).toBeVisible();
      });

      // When
      fireEvent.press(screen.getByTestId(TestTags.deleteButton(deleteTask.id)));

      // Then
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          expect.stringContaining(`/v1/tasks/${deleteTask.id}`),
          expect.objectContaining({ method: 'DELETE' }),
        );
      });
      expect(screen.getByTestId(TestTags.taskTitle(deleteTask.id))).toBeVisible();
      expect(screen.getByTestId(TestTags.taskTitle(keepTask.id))).toBeVisible();
      expect(screen.getByTestId(TestTags.ERROR_SNACKBAR)).toHaveTextContent(expectedError);
    });

    it('should allow delete retry after failure and remove task when retry succeeds', async () => {
      // Given
      const deleteTask = {
        id: 'retry-delete-target',
        title: 'Delete Me',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };
      const keepTask = {
        id: 'retry-keep-target',
        title: 'Keep Me',
        status: TaskStatus.DONE,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: { body: [deleteTask, keepTask] },
        },
      });
      const deleteSpy = jest
        .spyOn(taskRepository, 'deleteTask')
        .mockRejectedValueOnce(new ApiError(500, 'Delete failed'))
        .mockResolvedValueOnce(undefined);
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(deleteTask.id))).toBeVisible();
      });

      // When
      await pressDeleteWhenEnabled(screen, deleteTask.id);

      // Then
      await waitFor(() => {
        expect(deleteSpy).toHaveBeenCalledWith(deleteTask.id);
        expect(screen.getByTestId(TestTags.ERROR_SNACKBAR)).toHaveTextContent('Delete failed');
      });
      expect(screen.getByTestId(TestTags.taskTitle(deleteTask.id))).toBeVisible();

      // When
      await retryDeleteUntilTaskRemoved(screen, deleteTask.id);

      // Then
      expect(deleteSpy).toHaveBeenCalledTimes(2);
      expect(screen.getByTestId(TestTags.taskTitle(keepTask.id))).toBeVisible();
    });
  });

  describe('Pull-to-refresh tests', () => {
    it('should show new task when pull-to-refresh returns updated list', async () => {
      // Given
      const firstTask = {
        id: '1',
        title: 'First Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      const secondTask = {
        id: '2',
        title: 'Second Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: [{ body: [firstTask] }, { body: [firstTask, secondTask] }],
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(firstTask.id))).toBeVisible();
      });

      // When
      await pullToRefresh(screen);

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(secondTask.id))).toBeVisible();
      });
    });

    it('should keep empty list when pull-to-refresh returns empty list', async () => {
      // Given
      mockFetchResponse({
        '/v1/tasks': {
          GET: [{ body: [] }, { body: [] }],
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.EMPTY_TASKS)).toBeVisible();
      });

      // When
      await pullToRefresh(screen);

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.EMPTY_TASKS)).toBeVisible();
        expect(screen.getByTestId(TestTags.ADD_TASK_BUTTON)).toBeVisible();
      });
    });

    it('should keep existing tasks when pull-to-refresh returns same list', async () => {
      // Given
      const task = {
        id: '1',
        title: 'Stable Task',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: [{ body: [task] }, { body: [task] }],
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(task.id))).toBeVisible();
      });

      // When
      await pullToRefresh(screen);

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(task.id))).toBeVisible();
        expect(screen.queryByTestId(TestTags.taskTitle('2'))).toBeNull();
      });
    });

    it('should keep existing tasks when pull-to-refresh fails with server error', async () => {
      // Given
      const firstTask = {
        id: '1',
        title: 'Keep Me',
        status: TaskStatus.TODO,
        priority: TaskPriority.LOW,
      };
      const secondTask = {
        id: '2',
        title: 'Also Keep',
        status: TaskStatus.DONE,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        '/v1/tasks': {
          GET: [
            { body: [firstTask, secondTask] },
            { status: 500, ok: false, body: { message: 'Refresh failed' } },
          ],
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(firstTask.id))).toBeVisible();
      });

      // When
      await pullToRefresh(screen);

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.taskTitle(firstTask.id))).toBeVisible();
        expect(screen.getByTestId(TestTags.taskTitle(secondTask.id))).toBeVisible();
        expect(screen.getByTestId(TestTags.ERROR_SNACKBAR)).toHaveTextContent('Refresh failed');
      });
    });
  });

  describe('Create task tests', () => {
    it.each([
      { testCase: 'HTTP 500', getResponse: { status: 500, ok: false, body: { message: 'Server error' } } },
      { testCase: 'network error', getResponse: { reject: true } },
    ])('should allow opening create form when initial GET tasks fails with $testCase', async ({ getResponse }) => {
      // Given
      mockFetchResponse({
        '/v1/tasks': {
          GET: getResponse,
        },
      });
      const screen = renderWithProviders(
        <TaskListScreen navigation={navigation as never} route={route as never} />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId(TestTags.ADD_TASK_BUTTON)).toBeVisible();
      });

      // When
      fireEvent.press(screen.getByTestId(TestTags.ADD_TASK_BUTTON));

      // Then
      expect(navigation.navigate).toHaveBeenCalledWith('CreateTask');
    });
  });
});
