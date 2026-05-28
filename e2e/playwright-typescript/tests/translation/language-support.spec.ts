import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import { TaskPriority } from '@/data/TaskPriority';
import { TaskStatus } from '@/data/TaskStatus';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

test.beforeAll(async () => {
  await allure.epic(AllureEpic.TRANSLATION);
  await allure.feature('Language support');
  await allure.tms('104');
});

test.describe('Language support', () => {
  let context: TaskContext;

  test.beforeEach(async ({ support }) => {
    context = new TaskContext({ status: TaskStatus.TODO, priority: TaskPriority.LOW });
    const response = context.createTaskResponse();

    await support.mock.api.getTasks([response]);
    await support.mock.api.createTask(response);
  });

  test('should switch UI to Spanish when ES is selected', async ({ step, validate, support }) => {
    // given
    await step.navigation.openMainPage();

    // when
    await step.language.selectLanguage('ES');

    // then
    await validate.language.uiIsInSpanish();
    await validate.language.statusTagShowsText('TODO', 'Por hacer');
    await validate.language.priorityTagShowsText('LOW', 'Baja');
  });
});
