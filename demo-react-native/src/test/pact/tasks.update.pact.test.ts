import {MatchersV3} from '@pact-foundation/pact';

import {TaskPriority, TaskRequest, TaskStatus} from '@/data/models/task';
import {createTaskApi} from '@/data/remote/taskApi';
import {createPact} from './tasks.pact.fixtures';

const { like, regex, fromProviderState, extractPayload } = MatchersV3;

const pact = createPact('demo-service-tasks-update');
const taskId = '507f1f77bcf86cd799439011';
const timestampPattern = '^\\d{4}-\\d{2}-\\d{2}T.*$';
const taskPath = fromProviderState('/v1/tasks/${taskId}', `/v1/tasks/${taskId}`);

const requestContract: TaskRequest = {
  title: fromProviderState('${updatedTitle}', 'Prepare release notes - updated') as unknown as string,
  description: 'Document release tasks in detail',
  status: TaskStatus.IN_PROGRESS,
  priority: TaskPriority.HIGH,
};

describe('tasks PUT /v1/tasks/{id} pact', () => {
  it('should have update task success contract when updating task', async () => {
    await pact
      .addInteraction()
      .given('a task exists to update and title is unique')
      .uponReceiving('a valid task update request')
      .withRequest('PUT', taskPath, (req) => {
        req.headers({ 'Content-Type': 'application/json' });
        req.jsonBody(requestContract);
      })
      .willRespondWith(200, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody({
          id: regex('^[a-f0-9]{24}$', taskId),
          title: like(extractPayload(requestContract.title)),
          description: like(requestContract.description),
          status: like(requestContract.status),
          priority: like(requestContract.priority),
          createdDate: regex(timestampPattern, '2026-04-26T09:00:00.000Z'),
          updatedDate: regex(timestampPattern, '2026-04-26T10:30:00.000Z'),
        });
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await api.updateTask(taskId, extractPayload(requestContract) as TaskRequest);
      });
  });

  it('should have update task duplicate contract when updating with duplicate title', async () => {
    await pact
      .addInteraction()
      .given('another task has the requested title')
      .uponReceiving('a task update request with duplicate title')
      .withRequest('PUT', taskPath, (req) => {
        req.headers({ 'Content-Type': 'application/json' });
        req.jsonBody(requestContract);
      })
      .willRespondWith(409, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody({
          message: regex(
            "^Task with title '.*' already exists$",
            `Task with title '${extractPayload(requestContract.title)}' already exists`,
          ),
        });
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await expect(
          api.updateTask(taskId, extractPayload(requestContract) as TaskRequest),
        ).rejects.toThrow('already exists');
      });
  });
});
