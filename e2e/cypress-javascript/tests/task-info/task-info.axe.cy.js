const { TaskContext } = require('@/context/TaskContext');
const { AllureEpic } = require('@/data/AllureEpic');
const { step, validate, support } = require('@/fixtures/providers');

describe('View task info - accessibility', () => {
  let context;

  beforeEach(() => {
    cy.allure().epic(AllureEpic.ACCESSIBILITY);
    cy.allure().feature('View task info');
    cy.allure().tms('102');

    context = new TaskContext();
    const response = context.createTaskResponse();

    support.mock.api
      .getTasks([response])
      .getTask(response.id, response)
      .getIsValid(response.id, true);
  });

  it('should have no accessibility violations on task info modal', { tags: '@accessibility' }, () => {
    // given
    step.navigation.openMainPage();
    step.tasks.openTaskInfoForm(context.title);

    // when
    step.accessibility.analyze().then((results) => {
      // then
      validate.accessibility.hasNoViolations(results);
    });
  });
});
