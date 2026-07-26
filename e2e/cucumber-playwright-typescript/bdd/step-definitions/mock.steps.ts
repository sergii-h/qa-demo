import { TaskContext } from '@/context/TaskContext';
import { TaskPriority } from '@/data/TaskPriority';
import { TaskStatus } from '@/data/TaskStatus';
import { Given } from '@/fixtures';

Given('API mocks are set up for creating a task', async ({ support, scenario }) => {
  scenario.taskContext = new TaskContext();
  const response = scenario.taskContext.createTaskResponse();

  await support.mock.api.getTasks([response]);
  await support.mock.api.createTask(response);
  await support.mock.api.getTask(response.id, response);
  await support.mock.api.getIsValid(response.id, true);
});

Given('API mocks are set up for deleting a task', async ({ support, scenario }) => {
  scenario.taskContext = new TaskContext();
  const response = scenario.taskContext.createTaskResponse();

  await support.mock.api.getTasks([response]);
  await support.mock.api.deleteTask(response.id);
});

Given('API mocks are set up for editing a task', async ({ support, scenario }) => {
  scenario.taskContext = new TaskContext({
    status: TaskStatus.TODO,
    priority: TaskPriority.MEDIUM,
  });
  const response = scenario.taskContext.createTaskResponse();

  await support.mock.api.getTasks([response]);
  await support.mock.api.getTask(response.id, response);
  await support.mock.api.getIsValid(response.id, true);
});

Given('API mocks are set up for viewing task info', async ({ support, scenario }) => {
  scenario.taskContext = new TaskContext();
  const response = scenario.taskContext.createTaskResponse();

  await support.mock.api.getTasks([response]);
  await support.mock.api.getTask(response.id, response);
  await support.mock.api.getIsValid(response.id, true);
});

Given('API mocks return a task list with two tasks', async ({ support }) => {
  await support.mock.api.getTasks([
    new TaskContext().createTaskResponse(),
    new TaskContext().createTaskResponse(),
  ]);
});

Given('API mocks are set up for language support', async ({ support, scenario }) => {
  scenario.taskContext = new TaskContext({
    status: TaskStatus.TODO,
    priority: TaskPriority.LOW,
  });
  const response = scenario.taskContext.createTaskResponse();

  await support.mock.api.getTasks([response]);
  await support.mock.api.createTask(response);
});
