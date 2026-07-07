const { TaskContext } = require('@/context/TaskContext');
const { AllureEpic } = require('@/data/AllureEpic');
const { step, validate } = require('@/fixtures/providers');

describe('Create task', () => {
  beforeEach(() => {
    cy.allure().epic(AllureEpic.TASK_MANAGEMENT);
    cy.allure().feature('Create task');
    cy.allure().tms('100');
  });

  it('should create task', { tags: '@uat' }, () => {
    // given
    const context = new TaskContext();

    // when
    step.navigation.openMainPage();
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
