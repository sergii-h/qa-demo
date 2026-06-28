import {act, fireEvent, waitFor} from '@testing-library/react-native';

import {TaskListScreen} from './TaskListScreen';
import {taskRepository} from '@/repository/taskRepository';
import {mockTask} from '@/test-utils/taskFixtures';
import {TestTags} from '@/testTags';
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
      expect(getByTestId(TestTags.PAGE_TITLE)).toHaveTextContent('Tasks');
      expect(getByTestId(TestTags.TASK_LIST)).toBeVisible();
      expect(getByTestId(TestTags.taskTitle(mockTask.id))).toHaveTextContent(mockTask.title);
      expect(getByTestId(TestTags.statusTag(mockTask.status))).toHaveTextContent('To Do');
      expect(getByTestId(TestTags.priorityTag(mockTask.priority))).toHaveTextContent('Medium');
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
    expect(getByTestId(TestTags.LOADING_SPINNER)).toBeVisible();
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
      expect(getByTestId(TestTags.EMPTY_TASKS)).toHaveTextContent(
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
      expect(getByTestId(TestTags.taskTitle(mockTask.id))).toHaveTextContent(mockTask.title);
    });

    // When
    fireEvent.press(getByTestId(TestTags.infoButton(mockTask.id)));
    fireEvent.press(getByTestId(TestTags.editButton(mockTask.id)));
    fireEvent.press(getByTestId(TestTags.ADD_TASK_BUTTON));
    fireEvent.press(getByTestId(TestTags.deleteButton(mockTask.id)));

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
      expect(getByTestId(TestTags.ERROR_SNACKBAR)).toHaveTextContent('Something went wrong');
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
      expect(getByTestId(TestTags.taskTitle(mockTask.id))).toHaveTextContent(mockTask.title);
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
      expect(getByTestId(TestTags.REFRESHING)).toBeOnTheScreen();
    });

    await act(async () => {
      resolveRefresh([mockTask]);
    });

    await waitFor(() => {
      expect(queryByTestId(TestTags.REFRESHING)).toBeNull();
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
      expect(getByTestId(TestTags.ERROR_SNACKBAR)).toHaveTextContent('Load failed');
      expect(getByTestId(TestTags.ADD_TASK_BUTTON)).toBeVisible();
    });
  });
});
