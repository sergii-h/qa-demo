const { TaskContext } = require('@/context/TaskContext');
const { AllureEpic } = require('@/data/AllureEpic');
const { TaskPriority } = require('@/data/TaskPriority');
const { TaskStatus } = require('@/data/TaskStatus');
const { step, validate, support } = require('@/fixtures/providers');

describe('Language support', () => {
  let context;

  beforeEach(() => {
    cy.allure().epic(AllureEpic.TRANSLATION);
    cy.allure().feature('Language support');
    cy.allure().tms('104');

    context = new TaskContext({ status: TaskStatus.TODO, priority: TaskPriority.LOW });
    const response = context.createTaskResponse();

    support.mock.api
      .getTasks([response])
      .createTask(response);
  });

  it('should switch UI to Spanish when ES is selected', () => {
    // given
    step.navigation.openMainPage();

    // when
    step.language.selectLanguage('ES');

    // then
    validate.language.uiIsInSpanish();
    validate.language.statusTagShowsText('TODO', 'Por hacer');
    validate.language.priorityTagShowsText('LOW', 'Baja');
  });
});
