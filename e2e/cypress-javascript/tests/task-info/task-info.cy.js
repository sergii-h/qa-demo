const { TaskContext } = require('@/context/TaskContext');
const { AllureEpic } = require('@/data/AllureEpic');
const { step, validate, support } = require('@/fixtures/providers');

describe('View task info', () => {
  let context;

  beforeEach(() => {
    cy.allure().epic(AllureEpic.TASK_MANAGEMENT);
    cy.allure().feature('View task info');
    cy.allure().tms('102');

    context = new TaskContext();
    const response = context.createTaskResponse();

    support.mock.api
      .getTasks([response])
      .getTask(response.id, response)
      .getIsValid(response.id, true);
  });

  it('should view task info', () => {
    // given
    step.navigation.openMainPage();

    // when
    step.tasks.openTaskInfoForm(context.title);

    // then
    validate.task
      .data(context.createTaskData())
      .isValid();
  });
});
