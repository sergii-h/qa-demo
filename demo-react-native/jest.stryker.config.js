const baseConfig = require('./jest.config');

module.exports = {
  ...baseConfig,
  testMatch: ['**/*.unit.spec.(ts|tsx)'],
  collectCoverage: false,
  coverageThreshold: undefined,
};
