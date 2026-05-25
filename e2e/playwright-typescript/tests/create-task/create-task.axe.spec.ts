import { test } from '@/fixtures';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

test.beforeAll(async () => {
  await allure.epic(AllureEpic.ACCESSIBILITY);
  await allure.feature('Create task');
});

test.describe('Create task form - accessibility', () => {
  test('should have no accessibility violations on create task form', { tag: '@accessibility' }, async ({ step, validate }) => {
    // given
    await step.navigation.openMainPage();
    await step.tasks.openCreateTaskForm();

    // when
    const results = await step.accessibility.analyze();

    // then
    await validate.accessibility.hasNoViolations(results);
  });
});
