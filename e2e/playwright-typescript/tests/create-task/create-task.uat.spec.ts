import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

test.beforeAll(async () => {
  await allure.epic(AllureEpic.TASK_MANAGEMENT);
  await allure.feature('Create task');
});

test.describe('Create task', () => {
  test('should create task', { tag: '@uat' }, async ({ step, validate }) => {
    // given
    const context = new TaskContext();

    // when
    await step.navigation.openMainPage();
    await step.tasks.openCreateTaskForm();
    await step.tasks.createTask.fillForm(context.createTaskData());
    await step.tasks.createTask.submitForm();

    // then
    await validate.tasks.hasTask(context.title);

    // when
    await step.tasks.openTaskInfoForm(context.title);

    // then
    await validate.task.data(context.createTaskData());
  });
});
