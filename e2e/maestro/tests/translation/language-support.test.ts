import { TaskContext } from '@/context/TaskContext';
import { AllureEpic } from '@/data/AllureEpic';
import { TaskPriority } from '@/data/TaskPriority';
import { TaskStatus } from '@/data/TaskStatus';
import { maestroTestCase } from '@/runner/maestroTestCase';

export default maestroTestCase(import.meta.url, {
  name: 'should switch UI to Spanish when ES is selected',
  epic: AllureEpic.TRANSLATION,
  feature: 'Language support',
  tms: '104',
  suite: 'mocked',
  setupMocks: async (support) => {
    const taskContext = new TaskContext({
      status: TaskStatus.TODO,
      priority: TaskPriority.LOW,
    });
    const response = support.mock.api.fromResponse(taskContext.createTaskResponse());

    await support.reset();
    await support.mock.api.getTasks(response);
    await support.mock.api.getTasks(response);

    return taskContext.toMaestroEnv();
  },
});
