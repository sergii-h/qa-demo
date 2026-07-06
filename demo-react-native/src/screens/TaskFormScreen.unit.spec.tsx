import {fireEvent, waitFor} from '@testing-library/react-native';

import {TaskFormScreen} from './TaskFormScreen';
import {ApiError} from '@/data/remote/apiClient';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {taskRepository} from '@/repository/taskRepository';
import {mockTask} from '@/test-utils/taskFixtures';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

jest.mock('../repository/taskRepository', () => ({
  taskRepository: {
    getTask: jest.fn(),
    createTask: jest.fn(),
    updateTask: jest.fn(),
  },
}));

const navigation = {
  goBack: jest.fn(),
  navigate: jest.fn(),
};

describe('TaskFormScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.mocked(taskRepository.createTask).mockResolvedValue(mockTask);
    jest.mocked(taskRepository.updateTask).mockResolvedValue(mockTask);
  });

  it('should disable create button when title is empty', () => {
    // When
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    // Then
    expect(getByTestId('create-button').props.accessibilityState?.disabled).toBe(
      true,
    );
  });

  it('should disable save button when title is empty in edit mode', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockResolvedValue(mockTask);

    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{
          key: 'EditTask',
          name: 'EditTask',
          params: { taskId: 'task-1' },
        } as never}
      />,
    );

    await waitFor(() => {
      expect(getByTestId('edit-task-title-input')).toHaveDisplayValue(mockTask.title);
    });

    fireEvent.changeText(getByTestId('edit-task-title-input'), '');

    // Then
    expect(getByTestId('save-button').props.accessibilityState?.disabled).toBe(
      true,
    );
  });

  it('should show saving spinner when create is in progress', async () => {
    // Given
    let resolveCreate: (value: typeof mockTask) => void = () => undefined;
    jest.mocked(taskRepository.createTask).mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveCreate = resolve;
        }),
    );

    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.changeText(getByTestId('create-task-title-input'), 'New task');

    // When
    fireEvent.press(getByTestId('create-button'));

    // Then
    await waitFor(() => {
      expect(getByTestId('loading-spinner')).toBeVisible();
    });

    resolveCreate(mockTask);

    await waitFor(() => {
      expect(navigation.navigate).toHaveBeenCalledWith(
        'TaskList',
        expect.objectContaining({ refreshTrigger: expect.any(Number) }),
      );
    });
  });

  it('should show save error when save fails', async () => {
    // Given
    jest
      .mocked(taskRepository.createTask)
      .mockRejectedValue(new Error('Request failed (500)'));

    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.changeText(getByTestId('create-task-title-input'), 'New task');

    // When
    fireEvent.press(getByTestId('create-button'));

    // Then
    await waitFor(() => {
      expect(getByTestId('save-error')).toHaveTextContent('Request failed (500)');
    });
  });

  it('should clear title error when user types after validation error', async () => {
    // Given
    const { getByTestId, queryByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.changeText(getByTestId('create-task-title-input'), 'a'.repeat(101));
    fireEvent.press(getByTestId('create-button'));

    await waitFor(() => {
      expect(getByTestId('title-error')).toHaveTextContent(
        'Title must not exceed 100 characters',
      );
    });

    // When
    fireEvent.changeText(getByTestId('create-task-title-input'), 'Valid title');

    // Then
    await waitFor(() => {
      expect(queryByTestId('title-error')).toBeNull();
    });
  });

  it('should render create form', () => {
    // When
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    // Then
    expect(getByTestId('modal-title')).toHaveTextContent('New task');
    expect(getByTestId('create-task-title-input')).toHaveDisplayValue('');
    expect(getByTestId('create-button')).toHaveTextContent('Create');
  });

  it('should render edit form when editing', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockResolvedValue(mockTask);

    // When
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{
          key: 'EditTask',
          name: 'EditTask',
          params: { taskId: 'task-1' },
        } as never}
      />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId('modal-title')).toHaveTextContent('Edit task');
      expect(getByTestId('edit-task-title-input')).toHaveDisplayValue(mockTask.title);
      expect(getByTestId('save-button')).toHaveTextContent('Save');
    });
  });

  it('should show loading and error states when edit load fails', async () => {
    // Given
    jest
      .mocked(taskRepository.getTask)
      .mockImplementationOnce(() => new Promise(() => undefined))
      .mockRejectedValueOnce(new Error('Failed to load'));

    const { getByTestId, rerender } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'EditTask', name: 'EditTask', params: { taskId: 'task-1' } } as never}
      />,
    );

    expect(getByTestId('loading-spinner')).toBeVisible();

    // When
    rerender(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'EditTask', name: 'EditTask', params: { taskId: 'task-2' } } as never}
      />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId('load-error')).toHaveTextContent('Failed to load');
    });
  });

  it('should navigate to task list after successful save', async () => {
    // Given
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.changeText(getByTestId('create-task-title-input'), 'New task');

    // When
    fireEvent.press(getByTestId('create-button'));

    // Then
    await waitFor(() => {
      expect(taskRepository.createTask).toHaveBeenCalledWith({
        title: 'New task',
        description: null,
        status: TaskStatus.TODO,
        priority: TaskPriority.MEDIUM,
      });
      expect(navigation.navigate).toHaveBeenCalledWith(
        'TaskList',
        expect.objectContaining({ refreshTrigger: expect.any(Number) }),
      );
    });
  });

  it('should show duplicate title error when create returns 409', async () => {
    // Given
    jest
      .mocked(taskRepository.createTask)
      .mockRejectedValue(new ApiError(409, 'Request failed (409)'));
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.changeText(getByTestId('create-task-title-input'), 'Duplicate task');

    // When
    fireEvent.press(getByTestId('create-button'));

    // Then
    await waitFor(() => {
      expect(getByTestId('title-error')).toHaveTextContent(
        'Task with this title already exists',
      );
    });
  });

  it('should show duplicate title error when update returns 409', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockResolvedValue(mockTask);
    jest
      .mocked(taskRepository.updateTask)
      .mockRejectedValue(new ApiError(409, 'Request failed (409)'));
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{
          key: 'EditTask',
          name: 'EditTask',
          params: { taskId: 'task-1' },
        } as never}
      />,
    );

    await waitFor(() => {
      expect(getByTestId('edit-task-title-input')).toHaveDisplayValue(mockTask.title);
    });

    // When
    fireEvent.press(getByTestId('save-button'));

    // Then
    await waitFor(() => {
      expect(getByTestId('title-error')).toHaveTextContent(
        'Task with this title already exists',
      );
    });
  });

  it('should render create form with default status and priority', () => {
    // When
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    // Then
    expect(getByTestId('status-dropdown')).toHaveDisplayValue('To Do');
    expect(getByTestId('priority-dropdown')).toHaveDisplayValue('Medium');
  });

  it('should save task with selected status on create form', async () => {
    // Given
    const { getByTestId, findByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.press(getByTestId('status-dropdown-open'));
    fireEvent.press(await findByTestId(`status-dropdown-option-${TaskStatus.IN_PROGRESS}`));
    await waitFor(() => {
      expect(getByTestId('status-dropdown')).toHaveDisplayValue('In Progress');
    });

    fireEvent.changeText(getByTestId('create-task-title-input'), 'New task');

    // When
    fireEvent.press(getByTestId('create-button'));

    // Then
    await waitFor(() => {
      expect(taskRepository.createTask).toHaveBeenCalledWith({
        title: 'New task',
        description: null,
        status: TaskStatus.IN_PROGRESS,
        priority: TaskPriority.MEDIUM,
      });
    });
  });

  it('should save task with selected priority on create form', async () => {
    // Given
    const { getByTestId, findByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.press(getByTestId('priority-dropdown-open'));
    fireEvent.press(await findByTestId(`priority-dropdown-option-${TaskPriority.HIGH}`));
    await waitFor(() => {
      expect(getByTestId('priority-dropdown')).toHaveDisplayValue('High');
    });

    fireEvent.changeText(getByTestId('create-task-title-input'), 'New task');

    // When
    fireEvent.press(getByTestId('create-button'));

    // Then
    await waitFor(() => {
      expect(taskRepository.createTask).toHaveBeenCalledWith({
        title: 'New task',
        description: null,
        status: TaskStatus.TODO,
        priority: TaskPriority.HIGH,
      });
    });
  });

  it('should render edit form with loaded status and priority', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockResolvedValue({
      ...mockTask,
      status: TaskStatus.DONE,
      priority: TaskPriority.HIGH,
    });

    // When
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{
          key: 'EditTask',
          name: 'EditTask',
          params: { taskId: 'task-1' },
        } as never}
      />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId('status-dropdown')).toHaveDisplayValue('Done');
      expect(getByTestId('priority-dropdown')).toHaveDisplayValue('High');
    });
  });

  it('should navigate back when close button pressed', () => {
    // Given
    const { getByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    // When
    fireEvent.press(getByTestId('close-button'));

    // Then
    expect(navigation.goBack).toHaveBeenCalled();
  });
});
