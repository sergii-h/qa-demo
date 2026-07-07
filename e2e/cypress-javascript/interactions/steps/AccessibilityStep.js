const { step } = require('@/decorators/step-decorator');

class AccessibilityStep {
  constructor() {
    this.analyze = step('Analyze page accessibility with axe', this._analyze.bind(this));
  }

  _analyze() {
    cy.injectAxe();
    return cy.window().then((win) => win.axe.run());
  }
}

module.exports = { AccessibilityStep };
