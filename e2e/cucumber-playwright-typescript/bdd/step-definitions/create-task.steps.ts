import { When, Then } from '@/fixtures';

When('the user creates a task through the UI', async ({ step, scenario }) => {
  await step.tasks.openCreateTaskForm();
  await step.tasks.createTask.fillForm(scenario.taskContext.createTaskData());
  await step.tasks.createTask.submitForm();
});

Then('the task appears in the list', async ({ validate, scenario }) => {
  await validate.tasks.hasTask(scenario.taskContext.title);
});

When('the user opens task info for the created task', async ({ step, scenario }) => {
  await step.tasks.openTaskInfoForm(scenario.taskContext.title);
});

Then('the task details are displayed', async ({ validate, scenario }) => {
  await validate.task.data(scenario.taskContext.createTaskData());
});
