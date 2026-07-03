import { AllureEpic } from '@/data/AllureEpic';
import { maestroTestCase } from '@/runner/maestroTestCase';

export default maestroTestCase(import.meta.url, {
  name: 'should remove task from list when delete succeeds',
  epic: AllureEpic.TASK_MANAGEMENT,
  feature: 'Delete task',
  tms: '99',
  suite: 'mocked',
  setupMocks: async (support, context) => {
    const response = support.mock.api.fromResponse(context.createTaskResponse());

    await support.reset();
    await support.mock.api.getTasks(response);
    await support.mock.api.getTasks(response);
    await support.mock.api.deleteTask();

    return context.toMaestroEnv();
  },
});
