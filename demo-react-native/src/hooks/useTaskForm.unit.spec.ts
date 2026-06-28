import {renderHook, waitFor} from '@testing-library/react-native';

import {ApiError} from '@/data/remote/apiClient';
import {TaskPriority, TaskStatus} from '@/data/models/task';
import {TaskFormMode, useTaskForm} from './useTaskForm';
import {taskRepository} from '@/repository/taskRepository';
import {mockTask} from '@/test-utils/taskFixtures';
import {createI18nWrapper} from '@/test-utils/I18nWrapper';
import {runHookAction, runHookActionAsync} from '@/test-utils/runHookAction';

jest.mock('../repository/taskRepository', () => ({
  taskRepository: {
    getTask: jest.fn(),
    createTask: jest.fn(),
    updateTask: jest.fn(),
  },
}));

const wrapper = createI18nWrapper();

describe('useTaskForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should load task with null description as empty string', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockResolvedValue({
      ...mockTask,
      description: null,
    });

    // When
    const { result } = renderHook(
      () => useTaskForm(TaskFormMode.EDIT, 'task-1'),
      { wrapper },
    );

    // Then
    await waitFor(() => {
      expect(result.current.description).toBe('');
    });
  });

  it('should load task when editing', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockResolvedValue(mockTask);

    // When
    const { result } = renderHook(
      () => useTaskForm(TaskFormMode.EDIT, 'task-1'),
      { wrapper },
    );

    // Then
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    expect(result.current.title).toBe(mockTask.title);
    expect(result.current.description).toBe(mockTask.description);
    expect(result.current.status).toBe(mockTask.status);
    expect(result.current.priority).toBe(mockTask.priority);
  });

  it('should set load error when edit load fails', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockRejectedValue(new ApiError(404, 'missing'));

    // When
    const { result } = renderHook(
      () => useTaskForm(TaskFormMode.EDIT, 'task-1'),
      { wrapper },
    );

    // Then
    await waitFor(() => {
      expect(result.current.loadError).toBeTruthy();
    });
  });

  it('should update form fields', async () => {
    // Given
    const { result } = renderHook(() => useTaskForm(TaskFormMode.CREATE), { wrapper });

    // When
    runHookAction(() => {
      result.current.onTitleChange('New title');
      result.current.onDescriptionChange('Details');
      result.current.onStatusChange(TaskStatus.DONE);
      result.current.onPriorityChange(TaskPriority.HIGH);
    });

    // Then
    await waitFor(() => {
      expect(result.current.title).toBe('New title');
      expect(result.current.description).toBe('Details');
      expect(result.current.status).toBe(TaskStatus.DONE);
      expect(result.current.priority).toBe(TaskPriority.HIGH);
    });
  });

  it('should reject empty title on save', async () => {
    // Given
    const { result } = renderHook(() => useTaskForm(TaskFormMode.CREATE), { wrapper });

    // When
    await runHookActionAsync(() => result.current.save());

    // Then
    expect(taskRepository.createTask).not.toHaveBeenCalled();
  });

  it('should reject title longer than 100 characters', async () => {
    // Given
    const { result } = renderHook(() => useTaskForm(TaskFormMode.CREATE), { wrapper });

    runHookAction(() => {
      result.current.onTitleChange('a'.repeat(101));
    });

    await waitFor(() => {
      expect(result.current.title).toBe('a'.repeat(101));
    });

    // When
    await runHookActionAsync(() => result.current.save());

    // Then
    await waitFor(() => {
      expect(result.current.titleError).toBe('Title must not exceed 100 characters');
    });
    expect(taskRepository.createTask).not.toHaveBeenCalled();
  });

  it('should create task on save in create mode', async () => {
    // Given
    jest.mocked(taskRepository.createTask).mockResolvedValue(mockTask);
    const { result } = renderHook(() => useTaskForm(TaskFormMode.CREATE), { wrapper });

    runHookAction(() => {
      result.current.onTitleChange('Created task');
      result.current.onDescriptionChange('  ');
    });

    await waitFor(() => {
      expect(result.current.title).toBe('Created task');
    });

    // When
    await runHookActionAsync(() => result.current.save());

    // Then
    expect(taskRepository.createTask).toHaveBeenCalledWith({
      title: 'Created task',
      description: null,
      status: TaskStatus.TODO,
      priority: TaskPriority.MEDIUM,
    });
    await waitFor(() => {
      expect(result.current.saveSucceeded).toBe(true);
    });
  });

  it('should update task on save in edit mode', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockResolvedValue(mockTask);
    jest.mocked(taskRepository.updateTask).mockResolvedValue(mockTask);
    const { result } = renderHook(
      () => useTaskForm(TaskFormMode.EDIT, 'task-1'),
      { wrapper },
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    runHookAction(() => {
      result.current.onTitleChange('Updated title');
    });

    await waitFor(() => {
      expect(result.current.title).toBe('Updated title');
    });

    // When
    await runHookActionAsync(() => result.current.save());

    // Then
    expect(taskRepository.updateTask).toHaveBeenCalledWith(
      'task-1',
      expect.objectContaining({ title: 'Updated title' }),
    );
    await waitFor(() => {
      expect(result.current.saveSucceeded).toBe(true);
    });
  });

  it('should clear title error when title changed', async () => {
    // Given
    const { result } = renderHook(() => useTaskForm(TaskFormMode.CREATE), { wrapper });

    runHookAction(() => {
      result.current.onTitleChange('a'.repeat(101));
    });

    await waitFor(() => {
      expect(result.current.title).toBe('a'.repeat(101));
    });

    await runHookActionAsync(() => result.current.save());

    await waitFor(() => {
      expect(result.current.titleError).toBe('Title must not exceed 100 characters');
    });

    // When
    runHookAction(() => {
      result.current.onTitleChange('Valid title');
    });

    // Then
    await waitFor(() => {
      expect(result.current.titleError).toBeNull();
    });
  });

  it('should set duplicate title error on 409', async () => {
    // Given
    jest.mocked(taskRepository.createTask).mockRejectedValue(
      new ApiError(409, 'Request failed (409)'),
    );
    const { result } = renderHook(() => useTaskForm(TaskFormMode.CREATE), { wrapper });

    runHookAction(() => {
      result.current.onTitleChange('Duplicate');
    });

    await waitFor(() => {
      expect(result.current.title).toBe('Duplicate');
    });

    // When
    await runHookActionAsync(() => result.current.save());

    // Then
    await waitFor(() => {
      expect(result.current.titleError).toBe('Task with this title already exists');
      expect(result.current.saveError).toBeNull();
    });
  });

  it('should skip repository update when edit mode has no task id', async () => {
    // Given
    const { result } = renderHook(() => useTaskForm(TaskFormMode.EDIT), { wrapper });

    runHookAction(() => {
      result.current.onTitleChange('Updated title');
    });

    await waitFor(() => {
      expect(result.current.title).toBe('Updated title');
    });

    // When
    await runHookActionAsync(() => result.current.save());

    // Then
    expect(taskRepository.updateTask).not.toHaveBeenCalled();
    await waitFor(() => {
      expect(result.current.saveSucceeded).toBe(true);
    });
  });

  it('should set duplicate title error on update 409', async () => {
    // Given
    jest.mocked(taskRepository.getTask).mockResolvedValue(mockTask);
    jest.mocked(taskRepository.updateTask).mockRejectedValue(
      new ApiError(409, 'Request failed (409)'),
    );
    const { result } = renderHook(
      () => useTaskForm(TaskFormMode.EDIT, 'task-1'),
      { wrapper },
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    runHookAction(() => {
      result.current.onTitleChange('Duplicate');
    });

    await waitFor(() => {
      expect(result.current.title).toBe('Duplicate');
    });

    // When
    await runHookActionAsync(() => result.current.save());

    // Then
    await waitFor(() => {
      expect(result.current.titleError).toBe('Task with this title already exists');
    });
  });

  it('should set save error when save fails with non-409', async () => {
    // Given
    jest
      .mocked(taskRepository.createTask)
      .mockRejectedValue(new ApiError(500, 'Request failed (500)'));
    const { result } = renderHook(() => useTaskForm(TaskFormMode.CREATE), { wrapper });

    runHookAction(() => {
      result.current.onTitleChange('New task');
    });

    await waitFor(() => {
      expect(result.current.title).toBe('New task');
    });

    // When
    await runHookActionAsync(() => result.current.save());

    // Then
    await waitFor(() => {
      expect(result.current.saveError).toBe('Request failed (500)');
      expect(result.current.titleError).toBeNull();
    });
  });
});
