import { APIRequestContext } from '@playwright/test';
import { TaskRequest } from '@/data/TaskRequest';
import { testConfig } from '@/test.config';

type TaskResponse = {
  id: string;
  title: string;
  description: string;
  status: string;
  priority: string;
};

export class ApiClient {
  private readonly baseUrl = testConfig.services.api.url;

  constructor(private readonly request: APIRequestContext) {}

  async createTask(task: TaskRequest): Promise<TaskResponse> {
    const response = await this.request.post(`${this.baseUrl}/tasks`, {
      data: task,
    });
    return response.json();
  }

  async deleteTask(id: string): Promise<void> {
    await this.request.delete(`${this.baseUrl}/tasks/${id}`);
  }

  async getTasks(): Promise<TaskResponse[]> {
    const response = await this.request.get(`${this.baseUrl}/tasks`);
    return response.json();
  }
}
