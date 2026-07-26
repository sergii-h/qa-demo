import { TaskContext } from '@/context/TaskContext';
import { TaskPriority } from '@/data/TaskPriority';
import { TaskStatus } from '@/data/TaskStatus';
import { When, Then } from '@/fixtures';

When('the user edits the created task through the UI', async ({ step, scenario, support }) => {
  scenario.updatedTaskContext = new TaskContext({
    id: scenario.taskContext.id,
    title: `${scenario.taskContext.title}-Updated`,
    description: `${scenario.taskContext.description}-Updated`,
    status: TaskStatus.IN_PROGRESS,
    priority: TaskPriority.HIGH,
  });

  const updatedResponse = scenario.updatedTaskContext.createTaskResponse();

  await step.tasks.openEditTaskForm(scenario.taskContext.title);
  await step.tasks.editTask.fillForm(scenario.updatedTaskContext.createTaskData());

  await support.mock.api.updateTask(scenario.updatedTaskContext.id, updatedResponse);
  await support.mock.api.getTasks([updatedResponse]);
  await support.mock.api.getTask(scenario.updatedTaskContext.id, updatedResponse);
  await support.mock.api.getIsValid(scenario.updatedTaskContext.id, true);

  await step.tasks.editTask.submitForm();
});

When('the user opens task info for the updated task', async ({ step, scenario }) => {
  await step.tasks.openTaskInfoForm(scenario.updatedTaskContext!.title);
});

Then('the updated task details are displayed', async ({ validate, scenario }) => {
  await validate.task.data(scenario.updatedTaskContext!.createTaskData());
});
