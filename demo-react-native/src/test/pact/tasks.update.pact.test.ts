import {MatchersV3} from '@pact-foundation/pact';

import {ErrorResponse, Task, TaskPriority, TaskRequest, TaskStatus} from '@/data/models/task';
import {createTaskApi} from '@/data/remote/taskApi';
import {createPact, likeTask, TASK_ID} from './tasks.pact.fixtures';

const { regex, fromProviderState } = MatchersV3;

const pact = createPact('demo-service-tasks-update');
const taskPath = fromProviderState('/v1/tasks/${taskId}', `/v1/tasks/${TASK_ID}`);

const task: TaskRequest = {
  title: 'Prepare release notes - updated',
  description: 'Document release tasks in detail',
  status: TaskStatus.IN_PROGRESS,
  priority: TaskPriority.HIGH,
};

const requestBody: { [K in keyof TaskRequest]: unknown } = {
  title: fromProviderState('${updatedTitle}', task.title),
  description: task.description,
  status: task.status,
  priority: task.priority,
};

describe('tasks PUT /v1/tasks/{id} pact', () => {
  it('should have update task success contract when updating task', async () => {
    const updatedTask: Task = {
      id: TASK_ID,
      title: task.title,
      description: task.description,
      status: task.status,
      priority: task.priority,
      createdDate: '2026-04-26T09:00:00.000Z',
      updatedDate: '2026-04-26T10:30:00.000Z',
    };

    await pact
      .addInteraction()
      .given('a task exists to update and title is unique')
      .uponReceiving('a valid task update request')
      .withRequest('PUT', taskPath, (req) => {
        req.headers({ 'Content-Type': 'application/json' });
        req.jsonBody(requestBody);
      })
      .willRespondWith(200, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody(likeTask(updatedTask));
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await api.updateTask(TASK_ID, task);
      });
  });

  it('should have update task duplicate contract when updating with duplicate title', async () => {
    const errorBody: { [K in keyof Required<ErrorResponse>]: unknown } = {
      message: regex(
        "^Task with title '.*' already exists$",
        `Task with title '${task.title}' already exists`,
      ),
    };

    await pact
      .addInteraction()
      .given('another task has the requested title')
      .uponReceiving('a task update request with duplicate title')
      .withRequest('PUT', taskPath, (req) => {
        req.headers({ 'Content-Type': 'application/json' });
        req.jsonBody(requestBody);
      })
      .willRespondWith(409, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody(errorBody);
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await expect(api.updateTask(TASK_ID, task)).rejects.toThrow('already exists');
      });
  });
});
