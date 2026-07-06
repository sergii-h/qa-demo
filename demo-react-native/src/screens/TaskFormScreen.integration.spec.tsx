import {fireEvent, RenderAPI, waitFor} from '@testing-library/react-native';

import {TaskFormScreen} from './TaskFormScreen';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {mockFetchResponse} from '@/test-utils/mockFetch';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

async function selectEnumOption(
  screen: RenderAPI,
  dropdownTestId: string,
  optionTestId: string,
) {
  fireEvent.press(screen.getByTestId(`${dropdownTestId}-open`));
  fireEvent.press(await screen.findByTestId(optionTestId));
}

async function selectStatus(screen: RenderAPI, status: TaskStatus) {
  await selectEnumOption(
    screen,
    'status-dropdown',
    `status-dropdown-option-${status}`,
  );
}

async function selectPriority(screen: RenderAPI, priority: TaskPriority) {
  await selectEnumOption(
    screen,
    'priority-dropdown',
    `priority-dropdown-option-${priority}`,
  );
}

const navigation = {
  goBack: jest.fn(),
  navigate: jest.fn(),
};

const createRoute = {
  key: 'CreateTask',
  name: 'CreateTask' as const,
  params: undefined,
};

function editRoute(taskId: string) {
  return {
    key: 'EditTask',
    name: 'EditTask' as const,
    params: { taskId },
  };
}

describe('TaskFormScreen integration', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    global.fetch = jest.fn();
  });

  describe('Create task tests', () => {
    it('should create task with all values, send correct POST request and add new task to the list after successful response', async () => {
      // Given
      const createdTask = {
        id: 'task-123',
        title: 'Test Task',
        description: 'Test Description',
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        '/v1/tasks': {
          POST: { status: 201, body: createdTask },
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
      );

      fireEvent.changeText(screen.getByTestId('create-task-title-input'), 'Test Task');
      fireEvent.changeText(
        screen.getByTestId('task-description-input'),
        'Test Description',
      );
      await selectStatus(screen, TaskStatus.IN_PROGRESS);
      await waitFor(() => {
        expect(screen.getByTestId('status-dropdown')).toHaveDisplayValue('In Progress');
      });
      await selectPriority(screen, TaskPriority.HIGH);

      // When
      fireEvent.press(screen.getByTestId('create-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/v1/tasks'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({
            title: 'Test Task',
            description: 'Test Description',
            status: TaskStatus.IN_PROGRESS,
            priority: TaskPriority.HIGH,
          }),
        }),
      );
    });

    it('should create task with required values, send correct POST request and add new task to the list after successful response', async () => {
      // Given
      const createdTask = {
        id: 'task-124',
        title: 'Test Task',
        description: null,
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        '/v1/tasks': {
          POST: { status: 201, body: createdTask },
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
      );

      fireEvent.changeText(screen.getByTestId('create-task-title-input'), 'Test Task');

      // When
      fireEvent.press(screen.getByTestId('create-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/v1/tasks'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({
            title: 'Test Task',
            description: null,
            status: TaskStatus.TODO,
            priority: TaskPriority.MEDIUM,
          }),
        }),
      );
    });

    it('should allow successful creation after invalid title is corrected', async () => {
      // Given
      const createdTask = {
        id: 'task-130',
        title: 'Corrected title',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        '/v1/tasks': {
          POST: { status: 201, body: createdTask },
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
      );

      fireEvent.changeText(
        screen.getByTestId('create-task-title-input'),
        'a'.repeat(101),
      );
      fireEvent.press(screen.getByTestId('create-button'));
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toBeVisible();
      });

      // When
      fireEvent.changeText(
        screen.getByTestId('create-task-title-input'),
        'Corrected title',
      );
      await selectPriority(screen, TaskPriority.HIGH);
      fireEvent.press(screen.getByTestId('create-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
    });

    it('should not create a task when create form is closed without saving, and should reset form on reopen', async () => {
      // Given
      const screen = renderWithProviders(
        <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
      );

      fireEvent.changeText(
        screen.getByTestId('create-task-title-input'),
        'Temporary title',
      );
      fireEvent.changeText(
        screen.getByTestId('task-description-input'),
        'Temporary description',
      );

      // When
      fireEvent.press(screen.getByTestId('close-button'));
      screen.unmount();
      const reopened = renderWithProviders(
        <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
      );

      // Then
      expect(navigation.goBack).toHaveBeenCalled();
      expect(reopened.getByTestId('create-task-title-input').props.value).toBe('');
      expect(reopened.getByTestId('task-description-input').props.value).toBe('');
    });

    it('should allow retry and create task after initial POST failure', async () => {
      // Given
      const createdTask = {
        id: 'task-456',
        title: 'Retry Task',
        description: 'Retry Description',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        '/v1/tasks': {
          POST: [
            { status: 500, ok: false, body: { message: 'Server error' } },
            { status: 201, body: createdTask },
          ],
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
      );

      fireEvent.changeText(screen.getByTestId('create-task-title-input'), 'Retry Task');
      fireEvent.changeText(
        screen.getByTestId('task-description-input'),
        'Retry Description',
      );
      await selectPriority(screen, TaskPriority.HIGH);

      // When
      fireEvent.press(screen.getByTestId('create-button'));
      await waitFor(() => {
        expect(screen.getByTestId('save-error')).toBeVisible();
      });
      fireEvent.press(screen.getByTestId('create-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
    });

    it.each([
      { testCase: 'HTTP 500', refreshGetResponse: { status: 500, ok: false, body: { message: 'Refresh failed' } } },
      { testCase: 'network error', refreshGetResponse: { reject: true } },
    ])('should close create form when refresh GET fails with $testCase after successful POST', async ({ refreshGetResponse }) => {
      // Given
      const createdTask = {
        id: 'task-778',
        title: 'Task with refresh failure',
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      };
      mockFetchResponse({
        '/v1/tasks': {
          POST: { status: 201, body: createdTask },
          GET: refreshGetResponse,
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
      );

      fireEvent.changeText(
        screen.getByTestId('create-task-title-input'),
        'Task with refresh failure',
      );
      await selectPriority(screen, TaskPriority.HIGH);

      // When
      fireEvent.press(screen.getByTestId('create-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
    });

    it.each([
      { testCase: 'HTTP 400', postResponse: { status: 400, ok: false, body: { message: 'Request failed with 400' } } },
      { testCase: 'HTTP 500', postResponse: { status: 500, ok: false, body: { message: 'Request failed with 500' } } },
      { testCase: 'network error', postResponse: { reject: true } },
    ])('should display generic error on create form when POST request is rejected with $testCase', async ({ postResponse }) => {
      // Given
      mockFetchResponse({
        '/v1/tasks': {
          POST: postResponse,
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen navigation={navigation as never} route={createRoute as never} />,
      );

      fireEvent.changeText(screen.getByTestId('create-task-title-input'), 'Invalid Task');
      await selectPriority(screen, TaskPriority.HIGH);

      // When
      fireEvent.press(screen.getByTestId('create-button'));

      // Then
      await waitFor(() => {
        expect(screen.getByTestId('save-error')).toBeVisible();
        expect(screen.getByTestId('create-task-title-input')).toBeVisible();
      });
    });
  });

  describe('Edit task tests', () => {
    it('should update task with modified values, send correct PUT request and show modified task title in the list after successful response', async () => {
      // Given
      const originalTask = {
        id: 'task-1',
        title: 'Original title',
        description: 'Notes',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      const updatedTask = {
        id: 'task-1',
        title: 'Updated title',
        description: 'Notes',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${originalTask.id}`]: {
          GET: { body: originalTask },
          PUT: { body: updatedTask },
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen
          navigation={navigation as never}
          route={editRoute(originalTask.id) as never}
        />,
      );

      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });

      fireEvent.changeText(screen.getByTestId('edit-task-title-input'), 'Updated title');

      // When
      fireEvent.press(screen.getByTestId('save-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining(`/v1/tasks/${originalTask.id}`),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({
            title: 'Updated title',
            description: 'Notes',
            status: TaskStatus.TODO,
            priority: TaskPriority.MEDIUM,
          }),
        }),
      );
    });

    it('should update task with removed description', async () => {
      // Given
      const originalTask = {
        id: 'task-210',
        title: 'Task with description',
        description: 'Description to remove',
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.MEDIUM,
      };
      const updatedTask = {
        id: 'task-210',
        title: 'Task with description',
        description: null,
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${originalTask.id}`]: {
          GET: { body: originalTask },
          PUT: { body: updatedTask },
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen
          navigation={navigation as never}
          route={editRoute(originalTask.id) as never}
        />,
      );

      await waitFor(() => {
        expect(screen.getByTestId('task-description-input')).toBeVisible();
      });

      fireEvent.changeText(screen.getByTestId('task-description-input'), '');

      // When
      fireEvent.press(screen.getByTestId('save-button'));

      // Then
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          expect.stringContaining(`/v1/tasks/${originalTask.id}`),
          expect.objectContaining({
            method: 'PUT',
            body: JSON.stringify({
              title: 'Task with description',
              description: null,
              status: TaskStatus.IN_PROGRESS,
              priority: TaskPriority.MEDIUM,
            }),
          }),
        );
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
    });

    it('should not modify task when edit form is closed without saving', async () => {
      // Given
      const task = {
        id: 'task-1',
        title: 'Original title',
        description: 'Notes',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen
          navigation={navigation as never}
          route={editRoute(task.id) as never}
        />,
      );

      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });

      fireEvent.changeText(screen.getByTestId('edit-task-title-input'), 'Unsaved title');

      // When
      fireEvent.press(screen.getByTestId('close-button'));

      // Then
      expect(navigation.goBack).toHaveBeenCalled();
      expect(
        jest.mocked(global.fetch as jest.Mock).mock.calls.filter(
          ([, init]) => init?.method === 'PUT',
        ),
      ).toHaveLength(0);
    });

    it('should proceed with save after user corrects invalid title', async () => {
      // Given
      const originalTask = {
        id: 'task-1',
        title: 'Original title',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      const updatedTask = {
        id: 'task-1',
        title: 'Corrected title',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${originalTask.id}`]: {
          GET: { body: originalTask },
          PUT: { body: updatedTask },
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen
          navigation={navigation as never}
          route={editRoute(originalTask.id) as never}
        />,
      );

      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });

      fireEvent.changeText(screen.getByTestId('edit-task-title-input'), 'a'.repeat(101));
      fireEvent.press(screen.getByTestId('save-button'));
      await waitFor(() => {
        expect(screen.getByTestId('title-error')).toBeVisible();
      });

      // When
      fireEvent.changeText(screen.getByTestId('edit-task-title-input'), 'Corrected title');
      fireEvent.press(screen.getByTestId('save-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
    });

    it.each([
      { testCase: 'HTTP 500', getTaskResponse: { status: 500, ok: false, body: { message: 'Request failed with 500' } } },
      { testCase: 'network error', getTaskResponse: { reject: true } },
    ])('should allow opening edit form when initial GET tasks fails with $testCase', async ({ getTaskResponse }) => {
      // Given
      mockFetchResponse({
        '/v1/tasks/task-207': {
          GET: getTaskResponse,
        },
      });

      // When
      const screen = renderWithProviders(
        <TaskFormScreen
          navigation={navigation as never}
          route={editRoute('task-207') as never}
        />,
      );

      // Then
      await waitFor(() => {
        expect(screen.getByTestId('load-error')).toBeVisible();
      });
    });

    it.each([
      { testCase: 'HTTP 500', refreshGetResponse: { status: 500, ok: false, body: { message: 'Refresh failed' } } },
      { testCase: 'network error', refreshGetResponse: { reject: true } },
    ])('should close edit form when refresh GET fails with $testCase after successful PUT', async ({ refreshGetResponse }) => {
      // Given
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
          PUT: { body: updatedTask },
        },
        '/v1/tasks': {
          GET: refreshGetResponse,
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen
          navigation={navigation as never}
          route={editRoute(existingTask.id) as never}
        />,
      );

      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });

      fireEvent.changeText(
        screen.getByTestId('edit-task-title-input'),
        updatedTask.title,
      );

      // When
      fireEvent.press(screen.getByTestId('save-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
    });

    it('should allow retry and save task after initial PUT failure', async () => {
      // Given
      const originalTask = {
        id: 'task-1',
        title: 'Original title',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      const updatedTask = {
        id: 'task-1',
        title: 'Updated title',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${originalTask.id}`]: {
          GET: { body: originalTask },
          PUT: [
            { status: 500, ok: false, body: { message: 'Server error' } },
            { body: updatedTask },
          ],
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen
          navigation={navigation as never}
          route={editRoute(originalTask.id) as never}
        />,
      );

      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });

      fireEvent.changeText(screen.getByTestId('edit-task-title-input'), 'Updated title');

      // When
      fireEvent.press(screen.getByTestId('save-button'));
      await waitFor(() => {
        expect(screen.getByTestId('save-error')).toBeVisible();
      });
      fireEvent.press(screen.getByTestId('save-button'));

      // Then
      await waitFor(() => {
        expect(navigation.navigate).toHaveBeenCalledWith(
          'TaskList',
          expect.objectContaining({ refreshTrigger: expect.any(Number) }),
        );
      });
    });

    it.each([
      { testCase: 'HTTP 400', putResponse: { status: 400, ok: false, body: { message: 'Request failed with 400' } } },
      { testCase: 'HTTP 500', putResponse: { status: 500, ok: false, body: { message: 'Request failed with 500' } } },
      { testCase: 'network error', putResponse: { reject: true } },
    ])('should display generic error on edit form when PUT request is rejected with $testCase', async ({ putResponse }) => {
      // Given
      const task = {
        id: 'task-1',
        title: 'Original title',
        description: 'Notes',
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      };
      mockFetchResponse({
        [`/v1/tasks/${task.id}`]: {
          GET: { body: task },
          PUT: putResponse,
        },
      });
      const screen = renderWithProviders(
        <TaskFormScreen
          navigation={navigation as never}
          route={editRoute(task.id) as never}
        />,
      );

      await waitFor(() => {
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });

      fireEvent.changeText(screen.getByTestId('edit-task-title-input'), 'Updated title');

      // When
      fireEvent.press(screen.getByTestId('save-button'));

      // Then
      await waitFor(() => {
        expect(screen.getByTestId('save-error')).toBeVisible();
        expect(screen.getByTestId('edit-task-title-input')).toBeVisible();
      });
    });
  });
});
