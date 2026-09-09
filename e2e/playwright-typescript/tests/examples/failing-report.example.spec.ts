import { test } from '@/fixtures';
import { TaskContext } from '@/context/TaskContext';
import { TaskPriority } from '@/data/TaskPriority';
import * as allure from "allure-js-commons";
import { AllureEpic } from '@/data/AllureEpic';

// Intentional failure, kept on purpose: the GET /v1/tasks/{id} mock below returns a
// different priority than the one submitted through the form, simulating a stale
// API stub. Runs on both the Desktop Chrome and Mobile Safari projects, so the
// published report always shows a failure with screenshot, trace and network log
// for each — a live example of what a failing E2E report looks like and how to
// debug it
test.beforeAll(async () => {
  await allure.epic(AllureEpic.EXAMPLES);
  await allure.feature('Failing report example');
  await allure.tms('999');
});

test.describe('Example: failing report walkthrough', () => {
  let context: TaskContext;

  test.beforeEach(async ({ support }) => {
    context = new TaskContext({ priority: TaskPriority.MEDIUM });
    const response = context.createTaskResponse();
    const staleResponse = { ...response, priority: TaskPriority.HIGH };

    await support.mock.api.getTasks([response]);
    await support.mock.api.createTask(response);
    await support.mock.api.getTask(response.id, staleResponse);
    await support.mock.api.getIsValid(response.id, true);
  });

  test('should show a mismatched priority when the task API mock returns stale data', async ({ step, validate }) => {
    // given
    await step.navigation.openMainPage();

    // when
    await step.tasks.openCreateTaskForm();
    await step.tasks.createTask.fillForm(context.createTaskData());
    await step.tasks.createTask.submitForm();
    await step.tasks.openTaskInfoForm(context.title);

    // then
    await validate.task.data(context.createTaskData());
  });
});
