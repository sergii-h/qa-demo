jest.mock('./apiClient', () => ({
  API_BASE_URL: 'http://localhost:8080/v1/',
  apiRequest: jest.fn(),
}));

import {TaskPriority, TaskRequest, TaskStatus} from '../models/task';
import {API_BASE_URL, apiRequest} from './apiClient';
import {taskApi} from './taskApi';
import {mockTask} from '@/test-utils/taskFixtures';

describe('taskApi', () => {
  const request: TaskRequest = {
    title: mockTask.title,
    description: mockTask.description,
    status: TaskStatus.TODO,
    priority: TaskPriority.MEDIUM,
  };

  beforeEach(() => {
    jest.mocked(apiRequest).mockReset();
  });

  it('should get tasks', async () => {
    jest.mocked(apiRequest).mockResolvedValue([mockTask]);

    await expect(taskApi.getTasks()).resolves.toEqual([mockTask]);
    expect(apiRequest).toHaveBeenCalledWith(API_BASE_URL, 'tasks');
  });

  it('should get task by id', async () => {
    jest.mocked(apiRequest).mockResolvedValue(mockTask);

    await expect(taskApi.getTask('task-1')).resolves.toEqual(mockTask);
    expect(apiRequest).toHaveBeenCalledWith(API_BASE_URL, 'tasks/task-1');
  });

  it('should check task validity', async () => {
    jest.mocked(apiRequest).mockResolvedValue(true);

    await expect(taskApi.isValid('task-1')).resolves.toBe(true);
    expect(apiRequest).toHaveBeenCalledWith(API_BASE_URL, 'tasks/isValid/task-1');
  });

  it('should create task', async () => {
    jest.mocked(apiRequest).mockResolvedValue(mockTask);

    await expect(taskApi.createTask(request)).resolves.toEqual(mockTask);
    expect(apiRequest).toHaveBeenCalledWith(API_BASE_URL, 'tasks', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  });

  it('should update task', async () => {
    jest.mocked(apiRequest).mockResolvedValue(mockTask);

    await expect(taskApi.updateTask('task-1', request)).resolves.toEqual(mockTask);
    expect(apiRequest).toHaveBeenCalledWith(API_BASE_URL, 'tasks/task-1', {
      method: 'PUT',
      body: JSON.stringify(request),
    });
  });

  it('should delete task', async () => {
    jest.mocked(apiRequest).mockResolvedValue(undefined);

    await expect(taskApi.deleteTask('task-1')).resolves.toBeUndefined();
    expect(apiRequest).toHaveBeenCalledWith(API_BASE_URL, 'tasks/task-1', {
      method: 'DELETE',
    });
  });
});
