const { step } = require('@/decorators/step-decorator');

class NavigationStep {
  constructor() {
    this.openMainPage = step('Navigate to main page', () => cy.visit('/'));
    this.refresh = step('Refresh page', () => cy.reload());
  }
}

module.exports = { NavigationStep };
