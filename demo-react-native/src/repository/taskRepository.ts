import {Task, TaskRequest} from '@/data/models/task';
import {taskApi} from '@/data/remote/taskApi';

export const taskRepository = {
  getTasks: (): Promise<Task[]> => taskApi.getTasks(),

  getTask: (taskId: string): Promise<Task> => taskApi.getTask(taskId),

  isValid: (taskId: string): Promise<boolean> => taskApi.isValid(taskId),

  createTask: (request: TaskRequest): Promise<Task> =>
    taskApi.createTask(request),

  updateTask: (taskId: string, request: TaskRequest): Promise<Task> =>
    taskApi.updateTask(taskId, request),

  deleteTask: (taskId: string): Promise<void> => taskApi.deleteTask(taskId),
};
