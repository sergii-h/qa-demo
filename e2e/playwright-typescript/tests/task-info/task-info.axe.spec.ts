import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import { AllureEpic } from '@/data/AllureEpic';
import * as allure from "allure-js-commons";

test.beforeAll(async () => {
  await allure.epic(AllureEpic.ACCESSIBILITY);
  await allure.feature('View task info');
  await allure.tms('102');
});

test.describe('View task info - accessibility', () => {
  let context: TaskContext;

  test.beforeEach(async ({ support }) => {
    context = new TaskContext();
    const response = context.createTaskResponse();

    await support.mock.api.getTasks([response]);
    await support.mock.api.getTask(response.id, response);
    await support.mock.api.getIsValid(response.id, true);
  });

  test('should have no accessibility violations on task info modal', { tag: '@accessibility' }, async ({ step, validate }) => {
    // given
    await step.navigation.openMainPage();
    await step.tasks.openTaskInfoForm(context.title);

    // when
    const results = await step.accessibility.analyze();

    // then
    await validate.accessibility.hasNoViolations(results);
  });
});
