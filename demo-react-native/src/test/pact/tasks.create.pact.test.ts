import {MatchersV3} from '@pact-foundation/pact';

import {ErrorResponse, Task, TaskPriority, TaskRequest, TaskStatus} from '@/data/models/task';
import {createTaskApi} from '@/data/remote/taskApi';
import {createPact, likeTask, TASK_ID} from './tasks.pact.fixtures';

const { regex, fromProviderState } = MatchersV3;

const pact = createPact('demo-service-tasks-create');

const task: TaskRequest = {
  title: 'Prepare release notes',
  description: 'Document release tasks',
  status: TaskStatus.TODO,
  priority: TaskPriority.MEDIUM,
};

const requestBody: { [K in keyof TaskRequest]: unknown } = {
  title: fromProviderState('${taskTitle}', task.title),
  description: task.description,
  status: task.status,
  priority: task.priority,
};

describe('tasks POST /v1/tasks pact', () => {
  it('should have create task success contract when posting valid task', async () => {
    const createdTask: Task = {
      id: TASK_ID,
      title: task.title,
      description: task.description,
      status: task.status,
      priority: task.priority,
      createdDate: '2026-04-26T09:00:00.000Z',
      updatedDate: '2026-04-26T09:00:00.000Z',
    };

    await pact
      .addInteraction()
      .given('task title is unique')
      .uponReceiving('a valid task creation request')
      .withRequest('POST', '/v1/tasks', (req) => {
        req.headers({ 'Content-Type': 'application/json' });
        req.jsonBody(requestBody);
      })
      .willRespondWith(201, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody(likeTask(createdTask));
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await api.createTask(task);
      });
  });

  it('should have create task duplicate contract when posting duplicate title', async () => {
    const errorBody: { [K in keyof Required<ErrorResponse>]: unknown } = {
      message: regex(
        "^Task with title '.*' already exists$",
        `Task with title '${task.title}' already exists`,
      ),
    };

    await pact
      .addInteraction()
      .given('task title already exists')
      .uponReceiving('a task creation request with duplicate title')
      .withRequest('POST', '/v1/tasks', (req) => {
        req.headers({ 'Content-Type': 'application/json' });
        req.jsonBody(requestBody);
      })
      .willRespondWith(409, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody(errorBody);
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await expect(api.createTask(task)).rejects.toThrow('already exists');
      });
  });
});
