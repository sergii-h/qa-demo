import { AllureEpic } from '@/data/AllureEpic';
import { maestroTestCase } from '@/runner/maestroTestCase';

export default maestroTestCase(import.meta.url, {
  name: 'should have no accessibility violations on create task form',
  epic: AllureEpic.ACCESSIBILITY,
  feature: 'Create task',
  tms: '100',
  suite: 'accessibility',
  setupMocks: async (support) => {
    await support.reset();
    return {};
  },
});
