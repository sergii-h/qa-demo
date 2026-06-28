import {fireEvent, waitFor} from '@testing-library/react-native';

import {TaskFormScreen} from './TaskFormScreen';
import {ApiError} from '@/data/remote/apiClient';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {taskRepository} from '@/repository/taskRepository';
import {mockTask} from '@/test-utils/taskFixtures';
import {TestTags} from '@/testTags';
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
    expect(getByTestId(TestTags.CREATE_BUTTON).props.accessibilityState?.disabled).toBe(
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
      expect(getByTestId(TestTags.EDIT_TASK_TITLE_INPUT)).toHaveDisplayValue(mockTask.title);
    });

    fireEvent.changeText(getByTestId(TestTags.EDIT_TASK_TITLE_INPUT), '');

    // Then
    expect(getByTestId(TestTags.SAVE_BUTTON).props.accessibilityState?.disabled).toBe(
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

    fireEvent.changeText(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT), 'New task');

    // When
    fireEvent.press(getByTestId(TestTags.CREATE_BUTTON));

    // Then
    await waitFor(() => {
      expect(getByTestId(TestTags.LOADING_SPINNER)).toBeVisible();
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

    fireEvent.changeText(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT), 'New task');

    // When
    fireEvent.press(getByTestId(TestTags.CREATE_BUTTON));

    // Then
    await waitFor(() => {
      expect(getByTestId(TestTags.SAVE_ERROR)).toHaveTextContent('Request failed (500)');
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

    fireEvent.changeText(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT), 'a'.repeat(101));
    fireEvent.press(getByTestId(TestTags.CREATE_BUTTON));

    await waitFor(() => {
      expect(getByTestId(TestTags.TITLE_ERROR)).toHaveTextContent(
        'Title must not exceed 100 characters',
      );
    });

    // When
    fireEvent.changeText(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT), 'Valid title');

    // Then
    await waitFor(() => {
      expect(queryByTestId(TestTags.TITLE_ERROR)).toBeNull();
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
    expect(getByTestId(TestTags.MODAL_TITLE)).toHaveTextContent('New task');
    expect(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT)).toHaveDisplayValue('');
    expect(getByTestId(TestTags.CREATE_BUTTON)).toHaveTextContent('Create');
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
      expect(getByTestId(TestTags.MODAL_TITLE)).toHaveTextContent('Edit task');
      expect(getByTestId(TestTags.EDIT_TASK_TITLE_INPUT)).toHaveDisplayValue(mockTask.title);
      expect(getByTestId(TestTags.SAVE_BUTTON)).toHaveTextContent('Save');
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

    expect(getByTestId(TestTags.LOADING_SPINNER)).toBeVisible();

    // When
    rerender(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'EditTask', name: 'EditTask', params: { taskId: 'task-2' } } as never}
      />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId(TestTags.LOAD_ERROR)).toHaveTextContent('Failed to load');
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

    fireEvent.changeText(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT), 'New task');

    // When
    fireEvent.press(getByTestId(TestTags.CREATE_BUTTON));

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

    fireEvent.changeText(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT), 'Duplicate task');

    // When
    fireEvent.press(getByTestId(TestTags.CREATE_BUTTON));

    // Then
    await waitFor(() => {
      expect(getByTestId(TestTags.TITLE_ERROR)).toHaveTextContent(
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
      expect(getByTestId(TestTags.EDIT_TASK_TITLE_INPUT)).toHaveDisplayValue(mockTask.title);
    });

    // When
    fireEvent.press(getByTestId(TestTags.SAVE_BUTTON));

    // Then
    await waitFor(() => {
      expect(getByTestId(TestTags.TITLE_ERROR)).toHaveTextContent(
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
    expect(getByTestId(TestTags.STATUS_DROPDOWN)).toHaveDisplayValue('To Do');
    expect(getByTestId(TestTags.PRIORITY_DROPDOWN)).toHaveDisplayValue('Medium');
  });

  it('should save task with selected status on create form', async () => {
    // Given
    const { getByTestId, findByTestId, getAllByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.press(getAllByTestId('right-icon-adornment')[0]);
    fireEvent.press(await findByTestId(TestTags.statusDropdownOption(TaskStatus.IN_PROGRESS)));
    await waitFor(() => {
      expect(getByTestId(TestTags.STATUS_DROPDOWN)).toHaveDisplayValue('In Progress');
    });

    fireEvent.changeText(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT), 'New task');

    // When
    fireEvent.press(getByTestId(TestTags.CREATE_BUTTON));

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
    const { getByTestId, findByTestId, getAllByTestId } = renderWithProviders(
      <TaskFormScreen
        navigation={navigation as never}
        route={{ key: 'CreateTask', name: 'CreateTask', params: undefined } as never}
      />,
    );

    fireEvent.press(getAllByTestId('right-icon-adornment')[1]);
    fireEvent.press(await findByTestId(TestTags.priorityDropdownOption(TaskPriority.HIGH)));
    await waitFor(() => {
      expect(getByTestId(TestTags.PRIORITY_DROPDOWN)).toHaveDisplayValue('High');
    });

    fireEvent.changeText(getByTestId(TestTags.CREATE_TASK_TITLE_INPUT), 'New task');

    // When
    fireEvent.press(getByTestId(TestTags.CREATE_BUTTON));

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
      expect(getByTestId(TestTags.STATUS_DROPDOWN)).toHaveDisplayValue('Done');
      expect(getByTestId(TestTags.PRIORITY_DROPDOWN)).toHaveDisplayValue('High');
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
    fireEvent.press(getByTestId(TestTags.CLOSE_BUTTON));

    // Then
    expect(navigation.goBack).toHaveBeenCalled();
  });
});
