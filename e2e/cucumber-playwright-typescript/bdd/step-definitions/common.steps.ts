import { Given, When, Then } from '@/fixtures';

Given('the user is on the main page', async ({ step }) => {
  await step.navigation.openMainPage();
});

When('the user opens the create task form', async ({ step }) => {
  await step.tasks.openCreateTaskForm();
});

When('the page is analyzed for accessibility', async ({ step, scenario }) => {
  scenario.axeResults = await step.accessibility.analyze();
});

Then('there are no accessibility violations', async ({ validate, scenario }) => {
  await validate.accessibility.hasNoViolations(scenario.axeResults!);
});
