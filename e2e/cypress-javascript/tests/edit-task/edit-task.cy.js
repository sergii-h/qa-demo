const { TaskContext } = require('@/context/TaskContext');
const { AllureEpic } = require('@/data/AllureEpic');
const { TaskPriority } = require('@/data/TaskPriority');
const { TaskStatus } = require('@/data/TaskStatus');
const { step, validate, support } = require('@/fixtures/providers');

describe('Edit task', () => {
  let context;

  beforeEach(() => {
    cy.allure().epic(AllureEpic.TASK_MANAGEMENT);
    cy.allure().feature('Edit task');
    cy.allure().tms('101');

    context = new TaskContext({ status: TaskStatus.TODO, priority: TaskPriority.MEDIUM });
    const response = context.createTaskResponse();

    support.mock.api
      .getTasks([response])
      .getTask(response.id, response)
      .getIsValid(response.id, true);
  });

  it('should edit task', () => {
    // given
    const updatedContext = new TaskContext({
      id: context.id,
      title: `${context.title}-Updated`,
      description: `${context.description}-Updated`,
      status: TaskStatus.IN_PROGRESS,
      priority: TaskPriority.HIGH,
    });

    const updatedResponse = updatedContext.createTaskResponse();

    step.navigation.openMainPage();

    // when
    step.tasks.openEditTaskForm(context.title);
    step.tasks.editTask.fillForm(updatedContext.createTaskData());

    support.mock.api
      .updateTask(updatedContext.id, updatedResponse)
      .getTasks([updatedResponse])
      .getTask(updatedContext.id, updatedResponse)
      .getIsValid(updatedContext.id, true);

    step.tasks.editTask.submitForm();
    step.tasks.openTaskInfoForm(updatedContext.title);

    // then
    validate.task.data(updatedContext.createTaskData());
  });
});
