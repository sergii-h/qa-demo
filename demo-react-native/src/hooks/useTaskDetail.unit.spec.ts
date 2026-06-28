import {renderHook, waitFor} from '@testing-library/react-native';

import {ApiError} from '@/data/remote/apiClient';
import {useTaskDetail} from './useTaskDetail';
import {taskRepository} from '@/repository/taskRepository';
import {mockTask} from '@/test-utils/taskFixtures';
import {createI18nWrapper} from '@/test-utils/I18nWrapper';

jest.mock('../repository/taskRepository', () => ({
  taskRepository: {
    getTask: jest.fn(),
    isValid: jest.fn(),
  },
}));

const wrapper = createI18nWrapper();

describe('useTaskDetail', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should load task and validation status', async () => {
    jest.mocked(taskRepository.getTask).mockResolvedValue(mockTask);
    jest.mocked(taskRepository.isValid).mockResolvedValue(true);

    const { result } = renderHook(() => useTaskDetail('task-1'), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.task).toEqual(mockTask);
    expect(result.current.isValid).toBe(true);
    expect(result.current.errorMessage).toBeNull();
  });

  it('should set error when task load fails', async () => {
    jest.mocked(taskRepository.getTask).mockRejectedValue(new ApiError(404, 'missing'));
    jest.mocked(taskRepository.isValid).mockResolvedValue(false);

    const { result } = renderHook(() => useTaskDetail('task-1'), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.task).toBeNull();
    expect(result.current.errorMessage).toBeTruthy();
  });

  it('should default validation to false when validation request fails', async () => {
    jest.mocked(taskRepository.getTask).mockResolvedValue(mockTask);
    jest.mocked(taskRepository.isValid).mockRejectedValue(new Error('validation failed'));

    const { result } = renderHook(() => useTaskDetail('task-1'), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.task).toEqual(mockTask);
    expect(result.current.isValid).toBe(false);
  });
});
