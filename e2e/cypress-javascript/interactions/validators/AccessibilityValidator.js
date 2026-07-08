const { step } = require('@/decorators/step-decorator');

class AccessibilityValidator {
  constructor() {
    this.hasNoViolations = step('Validate no accessibility violations', this._hasNoViolations.bind(this));
  }

  _hasNoViolations(results) {
    cy.allure().attachment(
      'Axe accessibility report',
      JSON.stringify(results.violations, null, 2),
      'application/json',
    );
    expect(results.violations, 'Expected no accessibility violations').to.deep.equal([]);
  }
}

module.exports = { AccessibilityValidator };
