import { AllureEpic } from '@/data/AllureEpic';
import { maestroTestCase } from '@/runner/maestroTestCase';

export default maestroTestCase(import.meta.url, {
  name: 'should create task when form submitted with valid data',
  epic: AllureEpic.TASK_MANAGEMENT,
  feature: 'Create task',
  tms: '100',
  suite: 'uat',
});
