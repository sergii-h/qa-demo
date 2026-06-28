import {act, renderHook, waitFor} from '@testing-library/react-native';

import {ApiError} from '@/data/remote/apiClient';
import {useTaskList} from './useTaskList';
import {taskRepository} from '@/repository/taskRepository';
import {mockTask} from '@/test-utils/taskFixtures';
import {createI18nWrapper} from '@/test-utils/I18nWrapper';
import {runHookAction, runHookActionAsync} from '@/test-utils/runHookAction';

jest.mock('../repository/taskRepository', () => ({
  taskRepository: {
    getTasks: jest.fn(),
    deleteTask: jest.fn(),
  },
}));

const wrapper = createI18nWrapper();

describe('useTaskList', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.mocked(taskRepository.getTasks).mockResolvedValue([mockTask]);
  });

  it('should load tasks on mount', async () => {
    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.tasks).toEqual([mockTask]);
  });

  it('should set error when load fails', async () => {
    jest.mocked(taskRepository.getTasks).mockRejectedValue(new ApiError(500, 'fail'));

    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.errorMessage).toBeTruthy();
    });

    expect(result.current.tasks).toEqual([]);
  });

  it('should refresh tasks when refreshTrigger changes', async () => {
    const { result, rerender } = renderHook(
      ({ refreshTrigger }) => useTaskList(refreshTrigger),
      { initialProps: { refreshTrigger: 0 }, wrapper },
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    act(() => {
      rerender({ refreshTrigger: 1 });
    });

    await waitFor(() => {
      expect(taskRepository.getTasks).toHaveBeenCalledTimes(2);
    });
  });

  it('should replace tasks when refresh succeeds', async () => {
    const updatedTask = { ...mockTask, id: 'task-2', title: 'Updated list' };
    jest
      .mocked(taskRepository.getTasks)
      .mockResolvedValueOnce([mockTask])
      .mockResolvedValueOnce([updatedTask]);

    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.tasks).toEqual([mockTask]);
    });

    await runHookActionAsync(() => result.current.refreshTasks());

    await waitFor(() => {
      expect(result.current.tasks).toEqual([updatedTask]);
    });
  });

  it('should preserve tasks when refresh fails', async () => {
    jest
      .mocked(taskRepository.getTasks)
      .mockResolvedValueOnce([mockTask])
      .mockRejectedValueOnce(new ApiError(500, 'fail'));

    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    await runHookActionAsync(() => result.current.refreshTasks());

    await waitFor(() => {
      expect(result.current.errorMessage).toBeTruthy();
      expect(result.current.tasks).toEqual([mockTask]);
    });
  });

  it('should skip refresh when already refreshing', async () => {
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

    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    await act(async () => {
      const firstRefresh = result.current.refreshTasks();
      const secondRefresh = result.current.refreshTasks();
      resolveRefresh([mockTask]);
      await Promise.all([firstRefresh, secondRefresh]);
    });

    await waitFor(() => {
      expect(taskRepository.getTasks).toHaveBeenCalledTimes(2);
    });
  });

  it('should delete task and remove it from list', async () => {
    jest.mocked(taskRepository.deleteTask).mockResolvedValue(undefined);
    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.tasks).toEqual([mockTask]);
    });

    await runHookActionAsync(() => result.current.deleteTask(mockTask));

    await waitFor(() => {
      expect(result.current.tasks).toEqual([]);
    });
  });

  it('should set error when delete fails', async () => {
    jest.mocked(taskRepository.deleteTask).mockRejectedValue(new ApiError(500, 'fail'));
    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.tasks).toEqual([mockTask]);
    });

    await runHookActionAsync(() => result.current.deleteTask(mockTask));

    await waitFor(() => {
      expect(result.current.errorMessage).toBeTruthy();
      expect(result.current.tasks).toEqual([mockTask]);
    });
  });

  it('should remove task when delete retry succeeds after failure', async () => {
    jest
      .mocked(taskRepository.deleteTask)
      .mockRejectedValueOnce(new ApiError(500, 'Delete failed'))
      .mockResolvedValueOnce(undefined);
    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.tasks).toEqual([mockTask]);
    });

    await runHookActionAsync(() => result.current.deleteTask(mockTask));

    await waitFor(() => {
      expect(result.current.errorMessage).toBeTruthy();
      expect(result.current.tasks).toEqual([mockTask]);
    });

    await runHookActionAsync(() => result.current.deleteTask(mockTask));

    await waitFor(() => {
      expect(result.current.tasks).toEqual([]);
      expect(result.current.errorMessage).toBeNull();
    });
  });

  it('should ignore concurrent delete requests for the same task', async () => {
    let resolveDelete: (value: void) => void = () => undefined;
    jest.mocked(taskRepository.deleteTask).mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveDelete = resolve;
        }),
    );
    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.tasks).toEqual([mockTask]);
    });

    await act(async () => {
      const firstDelete = result.current.deleteTask(mockTask);
      const secondDelete = result.current.deleteTask(mockTask);
      resolveDelete();
      await Promise.all([firstDelete, secondDelete]);
    });

    await waitFor(() => {
      expect(taskRepository.deleteTask).toHaveBeenCalledTimes(1);
      expect(result.current.tasks).toEqual([]);
    });
  });

  it('should clear error message', async () => {
    jest.mocked(taskRepository.getTasks).mockRejectedValueOnce(new ApiError(500, 'fail'));
    const { result } = renderHook(() => useTaskList(), { wrapper });

    await waitFor(() => {
      expect(result.current.errorMessage).toBeTruthy();
    });

    runHookAction(() => {
      result.current.clearError();
    });

    await waitFor(() => {
      expect(result.current.errorMessage).toBeNull();
    });
  });
});
