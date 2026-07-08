require('@shelex/cypress-allure-plugin');
require('cypress-axe');

const registerCypressGrep = require('@cypress/grep');
const { resolveDevice } = require('@/data/devices');

registerCypressGrep();

before(function () {
  const device = Cypress.env('device') || 'desktop';
  const prefix = `[${device}] `;

  const prefixSuite = (suite) => {
    suite.tests.forEach((test) => {
      if (!test.title.startsWith(prefix)) {
        test.title = `${prefix}${test.title}`;
      }
    });
    suite.suites.forEach(prefixSuite);
  };

  prefixSuite(Cypress.mocha.getRunner().suite);
});

beforeEach(() => {
  const device = resolveDevice(Cypress.env('device'));
  cy.viewport(device.viewportWidth, device.viewportHeight);
});
