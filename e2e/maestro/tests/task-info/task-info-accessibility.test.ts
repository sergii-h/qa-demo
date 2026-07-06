import { AllureEpic } from '@/data/AllureEpic';
import { maestroTestCase } from '@/runner/maestroTestCase';

export default maestroTestCase(import.meta.url, {
  name: 'should have no accessibility violations on task info form',
  epic: AllureEpic.ACCESSIBILITY,
  feature: 'View task info',
  tms: '102',
  suite: 'accessibility',
  setupMocks: async (support, context) => {
    const response = support.mock.api.fromResponse(context.createTaskResponse());

    await support.reset();
    await support.mock.api.getTasks(response);
    await support.mock.api.getTasks(response);
    await support.mock.api.getTask(response);
    await support.mock.api.getIsValid(true);

    return context.toMaestroEnv();
  },
});
