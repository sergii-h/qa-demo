import { When, Then } from '@/fixtures';

When('the user deletes the created task', async ({ step, scenario }) => {
  await step.tasks.deleteTask(scenario.taskContext.title);
});

Then('the task is removed from the list', async ({ validate, scenario }) => {
  await validate.tasks.hasNoTask(scenario.taskContext.title);
});
