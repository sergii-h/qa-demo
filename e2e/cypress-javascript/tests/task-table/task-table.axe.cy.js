const { TaskContext } = require('@/context/TaskContext');
const { AllureEpic } = require('@/data/AllureEpic');
const { step, validate, support } = require('@/fixtures/providers');

describe('Task table - accessibility', () => {
  beforeEach(() => {
    cy.allure().epic(AllureEpic.ACCESSIBILITY);
    cy.allure().feature('Task table');
    cy.allure().tms('98');

    support.mock.api.getTasks([
      new TaskContext().createTaskResponse(),
      new TaskContext().createTaskResponse(),
    ]);
  });

  it('should have no accessibility violations on task table', { tags: '@accessibility' }, () => {
    // given
    step.navigation.openMainPage();

    // when
    step.accessibility.analyze().then((results) => {
      // then
      validate.accessibility.hasNoViolations(results);
    });
  });
});
