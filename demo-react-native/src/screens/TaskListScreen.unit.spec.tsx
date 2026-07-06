import {act, fireEvent, waitFor} from '@testing-library/react-native';

import {TaskListScreen} from './TaskListScreen';
import {taskRepository} from '@/repository/taskRepository';
import {mockTask} from '@/test-utils/taskFixtures';
import {renderWithProviders} from '@/test-utils/renderWithProviders';

jest.mock('../repository/taskRepository', () => ({
  taskRepository: {
    getTasks: jest.fn(),
    deleteTask: jest.fn(),
  },
}));

const navigation = {
  navigate: jest.fn(),
};

const route = {
  key: 'TaskList',
  name: 'TaskList' as const,
  params: undefined,
};

describe('TaskListScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.mocked(taskRepository.getTasks).mockResolvedValue([mockTask]);
  });

  it('should render task list with actions when tasks loaded', async () => {
    // When
    const { getByTestId } = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId('page-title')).toHaveTextContent('Tasks');
      expect(getByTestId('task-list')).toBeVisible();
      expect(getByTestId(`task-title-${mockTask.id}`)).toHaveTextContent(mockTask.title);
      expect(getByTestId(`status-tag-${mockTask.status}`)).toHaveTextContent('To Do');
      expect(getByTestId(`priority-tag-${mockTask.priority}`)).toHaveTextContent('Medium');
    });
  });

  it('should show loading spinner when loading', () => {
    // Given
    jest.mocked(taskRepository.getTasks).mockImplementation(
      () => new Promise(() => undefined),
    );

    // When
    const { getByTestId } = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    // Then
    expect(getByTestId('loading-spinner')).toBeVisible();
  });

  it('should show empty state when there are no tasks', async () => {
    // Given
    jest.mocked(taskRepository.getTasks).mockResolvedValue([]);

    // When
    const { getByTestId } = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId('empty-tasks')).toHaveTextContent(
        'No tasks yet. Tap + to create one.',
      );
    });
  });

  it('should navigate on task actions', async () => {
    // Given
    const { getByTestId } = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    await waitFor(() => {
      expect(getByTestId(`task-title-${mockTask.id}`)).toHaveTextContent(mockTask.title);
    });

    // When
    fireEvent.press(getByTestId(`info-button-${mockTask.id}`));
    fireEvent.press(getByTestId(`edit-button-${mockTask.id}`));
    fireEvent.press(getByTestId('add-task-button'));
    fireEvent.press(getByTestId(`delete-button-${mockTask.id}`));

    // Then
    expect(navigation.navigate).toHaveBeenCalledWith('TaskDetail', { taskId: mockTask.id });
    expect(navigation.navigate).toHaveBeenCalledWith('EditTask', { taskId: mockTask.id });
    expect(navigation.navigate).toHaveBeenCalledWith('CreateTask');
    await waitFor(() => {
      expect(taskRepository.deleteTask).toHaveBeenCalledWith(mockTask.id);
    });
  });

  it('should show snackbar when error message exists', async () => {
    // Given
    jest.mocked(taskRepository.getTasks).mockRejectedValue(new Error('Something went wrong'));

    // When
    const { getByTestId } = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId('error-snackbar')).toHaveTextContent('Something went wrong');
    });
  });

  it('should show refreshing indicator when refreshing', async () => {
    // Given
    let resolveRefresh: (value: typeof mockTask[]) => void = () => undefined;
    jest
      .mocked(taskRepository.getTasks)
      .mockResolvedValueOnce([mockTask])
      .mockImplementationOnce(
        () =>
          new Promise((resolve) => {
            resolveRefresh = resolve;
          }),
      );

    const { getByTestId, queryByTestId, rerender } = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    await waitFor(() => {
      expect(getByTestId(`task-title-${mockTask.id}`)).toHaveTextContent(mockTask.title);
    });

    // When
    rerender(
      <TaskListScreen
        navigation={navigation as never}
        route={{ ...route, params: { refreshTrigger: 1 } } as never}
      />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId('refreshing')).toBeOnTheScreen();
    });

    await act(async () => {
      resolveRefresh([mockTask]);
    });

    await waitFor(() => {
      expect(queryByTestId('refreshing')).toBeNull();
    });
  });

  it('should keep create entry point available when initial load fails', async () => {
    // Given
    jest.mocked(taskRepository.getTasks).mockRejectedValue(new Error('Load failed'));

    const { getByTestId } = renderWithProviders(
      <TaskListScreen navigation={navigation as never} route={route as never} />,
    );

    // Then
    await waitFor(() => {
      expect(getByTestId('error-snackbar')).toHaveTextContent('Load failed');
      expect(getByTestId('add-task-button')).toBeVisible();
    });
  });
});
