import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import { TaskPriority } from '@/data/TaskPriority';
import { TaskStatus } from '@/data/TaskStatus';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

test.beforeAll(async () => {
  await allure.epic(AllureEpic.TASK_MANAGEMENT);
  await allure.feature('Edit task');
  await allure.tms('101');
});

test.describe('Edit task', () => {
  let context: TaskContext;

  test.beforeEach(async ({ support }) => {
    context = new TaskContext({ status: TaskStatus.TODO, priority: TaskPriority.MEDIUM });
    const response = context.createTaskResponse();
    await support.mock.api.getTasks([response]);
    await support.mock.api.getTask(response.id, response);
    await support.mock.api.getIsValid(response.id, true);
  });

  test('should edit task', async ({ step, validate, support }) => {
    // given
    const updatedContext = new TaskContext({
      id: context.id,
      title: context.title + '-Updated',
      description: context.description + '-Updated',
      status: TaskStatus.IN_PROGRESS,
      priority: TaskPriority.HIGH,
    });

    const updatedResponse = updatedContext.createTaskResponse();

    await step.navigation.openMainPage();

    // when
    await step.tasks.openEditTaskForm(context.title);
    await step.tasks.editTask.fillForm(updatedContext.createTaskData());

    await support.mock.api.updateTask(updatedContext.id, updatedResponse);
    await support.mock.api.getTasks([updatedResponse]);
    await support.mock.api.getTask(updatedContext.id, updatedResponse);
    await support.mock.api.getIsValid(updatedContext.id, true);

    await step.tasks.editTask.submitForm();
    await step.tasks.openTaskInfoForm(updatedContext.title);

    // then
    await validate.task.data(updatedContext.createTaskData());
  });
});
