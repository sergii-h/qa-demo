const { step } = require('@/decorators/step-decorator');
const { MainPage } = require('@/interactions/pages/MainPage');

class LanguageValidator {
  constructor() {
    this.mainPage = new MainPage();
    this.uiIsInSpanish = step('Validate UI is in Spanish', this._uiIsInSpanish.bind(this));
    this.statusTagShowsText = step(
      'Validate status tag for {status} shows {expectedText}',
      this._statusTagShowsText.bind(this),
    );
    this.priorityTagShowsText = step(
      'Validate priority tag for {priority} shows {expectedText}',
      this._priorityTagShowsText.bind(this),
    );
  }

  _uiIsInSpanish() {
    this.mainPage.createTaskButton.should('have.text', 'Crear tarea');
    this.mainPage.tableHeaders.should('contain.text', 'Título');
    this.mainPage.tableHeaders.should('contain.text', 'Estado');
    this.mainPage.tableHeaders.should('contain.text', 'Prioridad');
  }

  _statusTagShowsText(status, expectedText) {
    this.mainPage.statusTag(status).should('have.text', expectedText);
  }

  _priorityTagShowsText(priority, expectedText) {
    this.mainPage.priorityTag(priority).should('have.text', expectedText);
  }
}

module.exports = { LanguageValidator };
