import {act, render, waitFor} from '@testing-library/react-native';

import {TaskDetailScreen} from '@/screens/TaskDetailScreen';
import {TaskFormScreen} from '@/screens/TaskFormScreen';
import {TaskListScreen} from '@/screens/TaskListScreen';
import {AppNavigator} from './AppNavigator';

jest.mock('../screens/TaskListScreen', () => ({
  TaskListScreen: jest.fn(() => null),
}));

jest.mock('../screens/TaskFormScreen', () => ({
  TaskFormScreen: jest.fn(() => null),
}));

jest.mock('../screens/TaskDetailScreen', () => ({
  TaskDetailScreen: jest.fn(() => null),
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
      expect(TaskListScreen).toHaveBeenCalled();
    });
  });

  it('should navigate to create task screen', async () => {
    // Given
    render(<AppNavigator />);
    const { navigation } = jest.mocked(TaskListScreen).mock.calls[0][0];

    // When
    act(() => {
      navigation.navigate('CreateTask');
    });

    // Then
    await waitFor(() => {
      expect(TaskFormScreen).toHaveBeenCalled();
    });
    const { route } = jest.mocked(TaskFormScreen).mock.calls.at(-1)![0];
    expect(route.name).toBe('CreateTask');
    expect(route.params).toBeUndefined();
  });

  it('should navigate to edit task screen', async () => {
    // Given
    render(<AppNavigator />);
    const { navigation } = jest.mocked(TaskListScreen).mock.calls[0][0];
    const taskId = 'task-1';

    // When
    act(() => {
      navigation.navigate('EditTask', { taskId });
    });

    // Then
    await waitFor(() => {
      expect(TaskFormScreen).toHaveBeenCalled();
    });
    const { route } = jest.mocked(TaskFormScreen).mock.calls.at(-1)![0];
    expect(route.name).toBe('EditTask');
    expect(route.params).toEqual({ taskId });
  });

  it('should navigate to task detail screen', async () => {
    // Given
    render(<AppNavigator />);
    const { navigation } = jest.mocked(TaskListScreen).mock.calls[0][0];
    const taskId = 'task-1';

    // When
    act(() => {
      navigation.navigate('TaskDetail', { taskId });
    });

    // Then
    await waitFor(() => {
      expect(TaskDetailScreen).toHaveBeenCalled();
    });
    const { route } = jest.mocked(TaskDetailScreen).mock.calls.at(-1)![0];
    expect(route.name).toBe('TaskDetail');
    expect(route.params).toEqual({ taskId });
  });
});
