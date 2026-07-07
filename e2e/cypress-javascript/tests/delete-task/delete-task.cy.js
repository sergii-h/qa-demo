const { TaskContext } = require('@/context/TaskContext');
const { AllureEpic } = require('@/data/AllureEpic');
const { step, validate, support } = require('@/fixtures/providers');

describe('Delete task', () => {
  let context;

  beforeEach(() => {
    cy.allure().epic(AllureEpic.TASK_MANAGEMENT);
    cy.allure().feature('Delete task');
    cy.allure().tms('99');

    context = new TaskContext();
    const response = context.createTaskResponse();

    support.mock.api
      .getTasks([response])
      .deleteTask(response.id);
  });

  it('should delete task', () => {
    // given
    step.navigation.openMainPage();

    // when
    step.tasks.deleteTask(context.title);

    // then
    validate.tasks.hasNoTask(context.title);
  });
});
