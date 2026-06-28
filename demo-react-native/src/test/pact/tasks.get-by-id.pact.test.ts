import {MatchersV3} from '@pact-foundation/pact';

import {TaskPriority, TaskStatus} from '@/data/models/task';
import {createTaskApi} from '@/data/remote/taskApi';
import {createPact} from './tasks.pact.fixtures';

const { like, regex, fromProviderState } = MatchersV3;

const pact = createPact('demo-service-tasks-get-by-id');
const taskId = '507f1f77bcf86cd799439011';
const timestampPattern = '^\\d{4}-\\d{2}-\\d{2}T.*$';
const taskPath = fromProviderState('/v1/tasks/${taskId}', `/v1/tasks/${taskId}`);

describe('tasks GET /v1/tasks/{id} pact', () => {
  it('should have get task by id contract when requesting task details', async () => {
    await pact
      .addInteraction()
      .given('a task exists')
      .uponReceiving('a request for a task by id')
      .withRequest('GET', taskPath, (req) => {
        req.headers({ 'Content-Type': 'application/json' });
      })
      .willRespondWith(200, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody({
          id: regex('^[a-f0-9]{24}$', taskId),
          title: like('Prepare release notes'),
          description: like('Document release tasks'),
          status: like(TaskStatus.TODO),
          priority: like(TaskPriority.MEDIUM),
          createdDate: regex(timestampPattern, '2026-04-26T09:00:00.000Z'),
          updatedDate: regex(timestampPattern, '2026-04-26T09:00:00.000Z'),
        });
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await api.getTask(taskId);
      });
  });
});
