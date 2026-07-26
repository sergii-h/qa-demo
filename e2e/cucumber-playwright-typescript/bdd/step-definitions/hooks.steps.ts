import * as allure from 'allure-js-commons';
import { AllureEpic } from '@/data/AllureEpic';
import { BeforeAll } from '@/fixtures';

BeforeAll({ tags: '@accessibility' }, async () => {
  await allure.epic(AllureEpic.ACCESSIBILITY);
});

BeforeAll({ tags: '@translation' }, async () => {
  await allure.epic(AllureEpic.TRANSLATION);
});

BeforeAll({ tags: 'not @accessibility and not @translation' }, async () => {
  await allure.epic(AllureEpic.TASK_MANAGEMENT);
});

BeforeAll({ tags: '@create-task' }, async () => {
  await allure.feature('Create task');
  await allure.tms('100');
});

BeforeAll({ tags: '@delete-task' }, async () => {
  await allure.feature('Delete task');
  await allure.tms('99');
});

BeforeAll({ tags: '@edit-task' }, async () => {
  await allure.feature('Edit task');
  await allure.tms('101');
});

BeforeAll({ tags: '@task-info' }, async () => {
  await allure.feature('View task info');
  await allure.tms('102');
});

BeforeAll({ tags: '@task-table' }, async () => {
  await allure.feature('Task table');
  await allure.tms('98');
});

BeforeAll({ tags: '@translation' }, async () => {
  await allure.feature('Language support');
  await allure.tms('104');
});
