import { AllureEpic } from '@/data/AllureEpic';
import { TaskPriority } from '@/data/TaskPriority';
import { TaskStatus } from '@/data/TaskStatus';
import { maestroTestCase } from '@/runner/maestroTestCase';

export default maestroTestCase(import.meta.url, {
  name: 'should update task when edit form submitted',
  epic: AllureEpic.TASK_MANAGEMENT,
  feature: 'Edit task',
  tms: '101',
  suite: 'mocked',
  setupMocks: async (support, context) => {
    const response = support.mock.api.fromResponse(context.createTaskResponse());
    const updatedContext = context.withUpdates({
      title: `${context.title}-Updated`,
      description: `${context.description}-Updated`,
      status: TaskStatus.IN_PROGRESS,
      priority: TaskPriority.HIGH,
    });
    const updatedResponse = support.mock.api.fromResponse(
      updatedContext.createTaskResponse(),
    );

    await support.reset();
    await support.mock.api.getTasks(response);
    await support.mock.api.getTask(response);
    await support.mock.api.getIsValid(true);
    await support.mock.api.updateTask(updatedResponse);
    await support.mock.api.getTasks(updatedResponse);
    await support.mock.api.getTasks(updatedResponse);
    await support.mock.api.getTask(updatedResponse);
    await support.mock.api.getIsValid(true);

    return {
      ...context.toMaestroEnv(),
      ...updatedContext.toMaestroEnv('UPDATED'),
    };
  },
});
