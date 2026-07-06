import { TaskContext } from '@/context/TaskContext';
import { AllureEpic } from '@/data/AllureEpic';
import { maestroTestCase } from '@/runner/maestroTestCase';

export default maestroTestCase(import.meta.url, {
  name: 'should have no accessibility violations on task table when tasks loaded',
  epic: AllureEpic.ACCESSIBILITY,
  feature: 'Task table',
  tms: '98',
  suite: 'accessibility',
  setupMocks: async (support) => {
    const first = new TaskContext();
    const second = new TaskContext();
    const tasks = [
      support.mock.api.fromResponse(first.createTaskResponse()),
      support.mock.api.fromResponse(second.createTaskResponse()),
    ];

    await support.reset();
    await support.mock.api.getTasks(...tasks);
    await support.mock.api.getTasks(...tasks);

    return {};
  },
});
