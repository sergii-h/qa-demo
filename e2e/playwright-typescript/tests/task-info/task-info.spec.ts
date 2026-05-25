import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

test.beforeAll(async () => {
  await allure.epic(AllureEpic.TASK_MANAGEMENT);
  await allure.feature('View task info');
});

test.describe('View task info', () => {
  let context: TaskContext;

  test.beforeEach(async ({ support }) => {
    context = new TaskContext();
    const response = context.createTaskResponse();

    await support.mock.api.getTasks([response]);
    await support.mock.api.getTask(response.id, response);
    await support.mock.api.getIsValid(response.id, true);
  });

  test('should view task info', async ({ step, validate }) => {
    // given
    await step.navigation.openMainPage();

    // when
    await step.tasks.openTaskInfoForm(context.title);

    // then
    await validate.task.data(context.createTaskData());
    await validate.task.isValid();
  });
});
