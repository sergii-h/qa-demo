import {act, render, waitFor} from '@testing-library/react-native';

import {AppNavigator} from './AppNavigator';

const mockTaskListScreen = jest.fn();
const mockTaskFormScreen = jest.fn();
const mockTaskDetailScreen = jest.fn();

jest.mock('../screens/TaskListScreen', () => ({
  TaskListScreen: function TaskListScreen(props: unknown) {
    mockTaskListScreen(props);
    return null;
  },
}));

jest.mock('../screens/TaskFormScreen', () => ({
  TaskFormScreen: function TaskFormScreen(props: unknown) {
    mockTaskFormScreen(props);
    return null;
  },
}));

jest.mock('../screens/TaskDetailScreen', () => ({
  TaskDetailScreen: function TaskDetailScreen(props: unknown) {
    mockTaskDetailScreen(props);
    return null;
  },
}));

describe('AppNavigator', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render task list as initial route', async () => {
    // When
    render(<AppNavigator />);

    // Then
    await waitFor(() => {
      expect(mockTaskListScreen).toHaveBeenCalled();
    });
  });

  it('should navigate to create task screen', async () => {
    // Given
    render(<AppNavigator />);
    const { navigation } = mockTaskListScreen.mock.calls[0][0];

    // When
    act(() => {
      navigation.navigate('CreateTask');
    });

    // Then
    await waitFor(() => {
      expect(mockTaskFormScreen).toHaveBeenCalled();
    });
    const { route } = mockTaskFormScreen.mock.calls.at(-1)![0];
    expect(route.name).toBe('CreateTask');
    expect(route.params).toBeUndefined();
  });

  it('should navigate to edit task screen', async () => {
    // Given
    render(<AppNavigator />);
    const { navigation } = mockTaskListScreen.mock.calls[0][0];
    const taskId = 'task-1';

    // When
    act(() => {
      navigation.navigate('EditTask', { taskId });
    });

    // Then
    await waitFor(() => {
      expect(mockTaskFormScreen).toHaveBeenCalled();
    });
    const { route } = mockTaskFormScreen.mock.calls.at(-1)![0];
    expect(route.name).toBe('EditTask');
    expect(route.params).toEqual({ taskId });
  });

  it('should navigate to task detail screen', async () => {
    // Given
    render(<AppNavigator />);
    const { navigation } = mockTaskListScreen.mock.calls[0][0];
    const taskId = 'task-1';

    // When
    act(() => {
      navigation.navigate('TaskDetail', { taskId });
    });

    // Then
    await waitFor(() => {
      expect(mockTaskDetailScreen).toHaveBeenCalled();
    });
    const { route } = mockTaskDetailScreen.mock.calls.at(-1)![0];
    expect(route.name).toBe('TaskDetail');
    expect(route.params).toEqual({ taskId });
  });
});
