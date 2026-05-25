import { Page, Route } from '@playwright/test';
import { TaskResponse } from '@/data/TaskResponse';

export class ApiRouteMock {
  constructor(private readonly page: Page) {}

  async createTask(response: TaskResponse): Promise<void> {
    await this.routeMethod('POST', '**/v1/tasks', (route) =>
      route.fulfill({ status: 201, json: response }),
    );
  }

  async getTasks(response: TaskResponse[]): Promise<void> {
    await this.routeMethod('GET', '**/v1/tasks', (route) =>
      route.fulfill({ status: 200, json: response }),
    );
  }

  async getTask(id: string, response: TaskResponse): Promise<void> {
    await this.routeMethod('GET', `**/v1/tasks/${id}`, (route) =>
      route.fulfill({ status: 200, json: response }),
    );
  }

  async deleteTask(id: string): Promise<void> {
    await this.routeMethod('DELETE', `**/v1/tasks/${id}`, (route) =>
      route.fulfill({ status: 204 }),
    );
  }

  async updateTask(id: string, response: TaskResponse): Promise<void> {
    await this.routeMethod('PUT', `**/v1/tasks/${id}`, (route) =>
      route.fulfill({ status: 200, json: response }),
    );
  }

  async getIsValid(id: string, isValid: boolean): Promise<void> {
    await this.routeMethod('GET', `**/v1/tasks/isValid/${id}`, (route) =>
      route.fulfill({ status: 200, json: isValid }),
    );
  }

  private async routeMethod(
    method: string,
    url: string,
    handler: (route: Route) => Promise<void>,
  ): Promise<void> {
    await this.page.route(url, async (route) => {
      if (route.request().method() !== method) {
        await route.fallback();
        return;
      }
      await handler(route);
    });
  }
}
