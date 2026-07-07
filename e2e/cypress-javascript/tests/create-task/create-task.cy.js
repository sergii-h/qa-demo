const { TaskContext } = require('@/context/TaskContext');
const { AllureEpic } = require('@/data/AllureEpic');
const { step, validate, support } = require('@/fixtures/providers');

describe('Create task', () => {
  let context;

  beforeEach(() => {
    cy.allure().epic(AllureEpic.TASK_MANAGEMENT);
    cy.allure().feature('Create task');
    cy.allure().tms('100');

    context = new TaskContext();
    const response = context.createTaskResponse();

    support.mock.api
      .getTasks([response])
      .createTask(response)
      .getTask(response.id, response)
      .getIsValid(response.id, true);
  });

  it('should create task', () => {
    // given
    step.navigation.openMainPage();

    // when
    step.tasks
      .openCreateTaskForm()
      .fillForm(context.createTaskData())
      .submitForm();

    // then
    validate.tasks.hasTask(context.title);

    // when
    step.tasks.openTaskInfoForm(context.title);

    // then
    validate.task.data(context.createTaskData());
  });
});
