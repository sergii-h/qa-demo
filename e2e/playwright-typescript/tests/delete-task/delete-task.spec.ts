import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

test.beforeAll(async () => {
  await allure.epic(AllureEpic.TASK_MANAGEMENT);
  await allure.feature('Delete task');
  await allure.tms('99');
});

test.describe('Delete task', () => {
  let context: TaskContext;

  test.beforeEach(async ({ support }) => {
    context = new TaskContext();
    const response = context.createTaskResponse();

    await support.mock.api.getTasks([response]);
    await support.mock.api.deleteTask(response.id);
  });

  test('should delete task', async ({ step, validate }) => {
    // given
    await step.navigation.openMainPage();

    // when
    await step.tasks.deleteTask(context.title);

    // then
    await validate.tasks.hasNoTask(context.title);
  });
});
