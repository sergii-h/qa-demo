import {MatchersV3} from '@pact-foundation/pact';

import {Task, TaskPriority, TaskStatus} from '@/data/models/task';
import {createTaskApi} from '@/data/remote/taskApi';
import {createPact, likeTask, TASK_ID} from './tasks.pact.fixtures';

const { eachLike } = MatchersV3;

const pact = createPact('demo-service-tasks-get-all');

const task: Task = {
  id: TASK_ID,
  title: 'Prepare release notes',
  description: 'Document release tasks',
  status: TaskStatus.TODO,
  priority: TaskPriority.MEDIUM,
  createdDate: '2026-04-26T09:00:00.000Z',
  updatedDate: '2026-04-26T09:00:00.000Z',
};

describe('tasks GET /v1/tasks pact', () => {
  it('should have get tasks contract when requesting all tasks', async () => {
    await pact
      .addInteraction()
      .given('tasks exist')
      .uponReceiving('a request for all tasks')
      .withRequest('GET', '/v1/tasks', (req) => {
        req.headers({ 'Content-Type': 'application/json' });
      })
      .willRespondWith(200, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody(eachLike(likeTask(task), 1));
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await api.getTasks();
      });
  });
});
