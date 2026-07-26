import { Then } from '@/fixtures';

Then('the task is marked as valid', async ({ validate }) => {
  await validate.task.isValid();
});
