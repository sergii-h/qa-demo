import { TaskResponse, toWireMockTask, WireMockTask } from '@/data/TaskResponse';
import { WireMockClient } from '@/support/mocks/WireMockClient';

export class ApiRouteMock {
  constructor(private readonly wireMock: WireMockClient) {}

  async getTasks(...tasks: WireMockTask[]): Promise<this> {
    await this.wireMock.addScenarioMapping({
      scenarioName: 'get-tasks',
      request: {
        method: 'GET',
        urlPath: TASKS_PATH,
      },
      response: jsonResponse(200, JSON.stringify(tasks)),
    });
    return this;
  }

  async getTask(task: WireMockTask): Promise<this> {
    await this.wireMock.addScenarioMapping({
      scenarioName: 'get-task',
      request: {
        method: 'GET',
        urlPathPattern: TASK_BY_ID_PATH,
      },
      response: jsonResponse(200, JSON.stringify(task)),
    });
    return this;
  }

  async getIsValid(isValid: boolean): Promise<this> {
    await this.wireMock.addScenarioMapping({
      scenarioName: 'is-valid',
      request: {
        method: 'GET',
        urlPathPattern: IS_VALID_PATH,
      },
      response: jsonResponse(200, JSON.stringify(isValid)),
    });
    return this;
  }

  async createTask(task: WireMockTask): Promise<this> {
    await this.wireMock.addScenarioMapping({
      scenarioName: 'create-task',
      request: {
        method: 'POST',
        urlPath: TASKS_PATH,
      },
      response: jsonResponse(200, JSON.stringify(task)),
    });
    return this;
  }

  async updateTask(task: WireMockTask): Promise<this> {
    await this.wireMock.addScenarioMapping({
      scenarioName: 'update-task',
      request: {
        method: 'PUT',
        urlPathPattern: TASK_BY_ID_PATH,
      },
      response: jsonResponse(200, JSON.stringify(task)),
    });
    return this;
  }

  async deleteTask(): Promise<this> {
    await this.wireMock.addScenarioMapping({
      scenarioName: 'delete-task',
      request: {
        method: 'DELETE',
        urlPathPattern: TASK_BY_ID_PATH,
      },
      response: { status: 204 },
    });
    return this;
  }

  fromResponse(response: TaskResponse): WireMockTask {
    return toWireMockTask(response);
  }
}

function jsonResponse(status: number, body: string): Record<string, unknown> {
  return {
    status,
    body,
    headers: { 'Content-Type': 'application/json' },
  };
}

const TASKS_PATH = '/v1/tasks';
const TASK_BY_ID_PATH = '/v1/tasks/(?!isValid)[^/]+';
const IS_VALID_PATH = '/v1/tasks/isValid/.+';
