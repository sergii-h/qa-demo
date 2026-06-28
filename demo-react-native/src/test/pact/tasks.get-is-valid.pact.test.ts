import {MatchersV3} from '@pact-foundation/pact';

import {createTaskApi} from '@/data/remote/taskApi';
import {createPact} from './tasks.pact.fixtures';

const { boolean, fromProviderState } = MatchersV3;

const pact = createPact('demo-service-tasks-get-is-valid');
const taskId = '507f1f77bcf86cd799439011';
const isValidPath = fromProviderState(
  '/v1/tasks/isValid/${taskId}',
  `/v1/tasks/isValid/${taskId}`,
);

describe('tasks GET /v1/tasks/isValid/{id} pact', () => {
  it('should have get is valid contract', async () => {
    await pact
      .addInteraction()
      .given('validation result is true for the task')
      .uponReceiving('a request for task validation status')
      .withRequest('GET', isValidPath, (req) => {
        req.headers({ 'Content-Type': 'application/json' });
      })
      .willRespondWith(200, (res) => {
        res.headers({ 'Content-Type': 'application/json' });
        res.jsonBody(boolean(true));
      })
      .executeTest(async (mockServer) => {
        const api = createTaskApi(`${mockServer.url}/v1/`);
        await api.isValid(taskId);
      });
  });
});
