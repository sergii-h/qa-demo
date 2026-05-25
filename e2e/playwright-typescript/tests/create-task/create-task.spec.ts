import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

test.beforeAll(async () => {
  await allure.epic(AllureEpic.TASK_MANAGEMENT);
  await allure.feature('Create task');
});

test.describe('Create task', () => {
  let context: TaskContext;

  test.beforeEach(async ({ support }) => {
    context = new TaskContext();
    const response = context.createTaskResponse();

    await support.mock.api.getTasks([response]);
    await support.mock.api.createTask(response);
    await support.mock.api.getTask(response.id, response);
    await support.mock.api.getIsValid(response.id, true);
  });

  test('should create task', async ({ step, validate }) => {
    // given
    await step.navigation.openMainPage();

    // when
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
