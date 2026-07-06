import { AllureEpic } from '@/data/AllureEpic';
import { maestroTestCase } from '@/runner/maestroTestCase';

export default maestroTestCase(import.meta.url, {
  name: 'should create task when form submitted with valid data',
  epic: AllureEpic.TASK_MANAGEMENT,
  feature: 'Create task',
  tms: '100',
  suite: 'mocked',
  setupMocks: async (support, context) => {
    const response = support.mock.api.fromResponse(context.createTaskResponse());

    await support.reset();
    await support.mock.api.getTasks(response);
    await support.mock.api.createTask(response);
    await support.mock.api.getTasks(response);
    await support.mock.api.getTasks(response);
    await support.mock.api.getTasks(response);
    await support.mock.api.getTask(response);
    await support.mock.api.getIsValid(true);

    return context.toMaestroEnv();
  },
});
