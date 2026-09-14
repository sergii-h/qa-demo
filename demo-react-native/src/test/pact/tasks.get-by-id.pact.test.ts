import {MatchersV3} from '@pact-foundation/pact';

import {Task, TaskPriority, TaskStatus} from '@/data/models/task';
import {createTaskApi} from '@/data/remote/taskApi';
import {createPact, likeTask, TASK_ID} from './tasks.pact.fixtures';

const { fromProviderState } = MatchersV3;

const pact = createPact('demo-service-tasks-get-by-id');
const taskPath = fromProviderState('/v1/tasks/${taskId}', `/v1/tasks/${TASK_ID}`);

const task: Task = {
  id: TASK_ID,
  title: 'Prepare release notes',
  description: 'Document release tasks',
  status: TaskStatus.TODO,
  priority: TaskPriority.MEDIUM,
  createdDate: '2026-04-26T09:00:00.000Z',
  updatedDate: '2026-04-26T09:00:00.000Z',
};

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
        res.jsonBody(likeTask(task));
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await api.getTask(TASK_ID);
      });
  });
});
