import {Task, TaskRequest} from '../models/task';
import {API_BASE_URL, apiRequest} from './apiClient';

export function createTaskApi(baseUrl: string) {
  return {
    getTasks: (): Promise<Task[]> => apiRequest<Task[]>(baseUrl, 'tasks'),

    getTask: (taskId: string): Promise<Task> =>
      apiRequest<Task>(baseUrl, `tasks/${taskId}`),

    isValid: (taskId: string): Promise<boolean> =>
      apiRequest<boolean>(baseUrl, `tasks/isValid/${taskId}`),

    createTask: (request: TaskRequest): Promise<Task> =>
      apiRequest<Task>(baseUrl, 'tasks', {
        method: 'POST',
        body: JSON.stringify(request),
      }),

    updateTask: (taskId: string, request: TaskRequest): Promise<Task> =>
      apiRequest<Task>(baseUrl, `tasks/${taskId}`, {
        method: 'PUT',
        body: JSON.stringify(request),
      }),

    deleteTask: (taskId: string): Promise<void> =>
      apiRequest<void>(baseUrl, `tasks/${taskId}`, { method: 'DELETE' }),
  };
}

export const taskApi = createTaskApi(API_BASE_URL);
