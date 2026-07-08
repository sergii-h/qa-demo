const { AllureEpic } = require('@/data/AllureEpic');
const { step, validate } = require('@/fixtures/providers');

describe('Create task form - accessibility', () => {
  beforeEach(() => {
    cy.allure().epic(AllureEpic.ACCESSIBILITY);
    cy.allure().feature('Create task');
    cy.allure().tms('100');
  });

  it('should have no accessibility violations on create task form', { tags: '@accessibility' }, () => {
    // given
    step.navigation.openMainPage();
    step.tasks.openCreateTaskForm();

    // when
    step.accessibility.analyze().then((results) => {
      // then
      validate.accessibility.hasNoViolations(results);
    });
  });
});
