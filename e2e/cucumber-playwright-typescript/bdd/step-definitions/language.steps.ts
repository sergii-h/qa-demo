import { When, Then } from '@/fixtures';

When('the user selects Spanish language', async ({ step }) => {
  await step.language.selectLanguage('ES');
});

Then('the UI is displayed in Spanish', async ({ validate }) => {
  await validate.language.uiIsInSpanish();
});

Then('the status tag shows Spanish text for TODO', async ({ validate }) => {
  await validate.language.statusTagShowsText('TODO', 'Por hacer');
});

Then('the priority tag shows Spanish text for LOW', async ({ validate }) => {
  await validate.language.priorityTagShowsText('LOW', 'Baja');
});
