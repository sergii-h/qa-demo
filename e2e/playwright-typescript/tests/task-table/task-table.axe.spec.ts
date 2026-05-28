import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

test.beforeAll(async () => {
  await allure.epic(AllureEpic.ACCESSIBILITY);
  await allure.feature('Task table');
  await allure.tms('98');
});

test.describe('Task table - accessibility', () => {
  test.beforeEach(async ({ support }) => {
    await support.mock.api.getTasks([
      new TaskContext().createTaskResponse(),
      new TaskContext().createTaskResponse(),
    ]);
  });

  test('should have no accessibility violations on task table', { tag: '@accessibility' }, async ({ step, validate }) => {
    // given
    await step.navigation.openMainPage();

    // when
    const results = await step.accessibility.analyze();

    // then
    await validate.accessibility.hasNoViolations(results);
  });
});
