import {TaskPriority, TaskRequest, TaskStatus} from '@/data/models/task';
import {taskApi} from '@/data/remote/taskApi';
import {taskRepository} from './taskRepository';
import {mockTask} from '@/test-utils/taskFixtures';

jest.mock('../data/remote/taskApi', () => ({
  taskApi: {
    getTasks: jest.fn(),
    getTask: jest.fn(),
    isValid: jest.fn(),
    createTask: jest.fn(),
    updateTask: jest.fn(),
    deleteTask: jest.fn(),
  },
}));

describe('taskRepository', () => {
  const request: TaskRequest = {
    title: mockTask.title,
    description: mockTask.description,
    status: TaskStatus.TODO,
    priority: TaskPriority.MEDIUM,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should delegate getTasks to taskApi', async () => {
    jest.mocked(taskApi.getTasks).mockResolvedValue([mockTask]);

    await expect(taskRepository.getTasks()).resolves.toEqual([mockTask]);
    expect(taskApi.getTasks).toHaveBeenCalled();
  });

  it('should delegate getTask to taskApi', async () => {
    jest.mocked(taskApi.getTask).mockResolvedValue(mockTask);

    await expect(taskRepository.getTask('task-1')).resolves.toEqual(mockTask);
    expect(taskApi.getTask).toHaveBeenCalledWith('task-1');
  });

  it('should delegate isValid to taskApi', async () => {
    jest.mocked(taskApi.isValid).mockResolvedValue(true);

    await expect(taskRepository.isValid('task-1')).resolves.toBe(true);
    expect(taskApi.isValid).toHaveBeenCalledWith('task-1');
  });

  it('should delegate createTask to taskApi', async () => {
    jest.mocked(taskApi.createTask).mockResolvedValue(mockTask);

    await expect(taskRepository.createTask(request)).resolves.toEqual(mockTask);
    expect(taskApi.createTask).toHaveBeenCalledWith(request);
  });

  it('should delegate updateTask to taskApi', async () => {
    jest.mocked(taskApi.updateTask).mockResolvedValue(mockTask);

    await expect(taskRepository.updateTask('task-1', request)).resolves.toEqual(mockTask);
    expect(taskApi.updateTask).toHaveBeenCalledWith('task-1', request);
  });

  it('should delegate deleteTask to taskApi', async () => {
    jest.mocked(taskApi.deleteTask).mockResolvedValue(undefined);

    await expect(taskRepository.deleteTask('task-1')).resolves.toBeUndefined();
    expect(taskApi.deleteTask).toHaveBeenCalledWith('task-1');
  });
});
