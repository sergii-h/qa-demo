const path = require('path');
const fs = require('fs');
const { defineConfig } = require('cypress');
const dotenv = require('dotenv');
const grep = require('@cypress/grep/src/plugin');
const allureWriter = require('@shelex/cypress-allure-plugin/writer');
const webpackPreprocessor = require('@cypress/webpack-preprocessor');
const cypressSplit = require('cypress-split');
const testConfig = require('./test.config');

const envPath = fs.existsSync(path.resolve(__dirname, '.env.e2e.local'))
  ? path.resolve(__dirname, '.env.e2e.local')
  : path.resolve(__dirname, '.env.e2e');

dotenv.config({ path: envPath });

const SUITE_CONFIG = {
  e2e: {
    specPattern: 'tests/**/*.cy.js',
    excludeSpecPattern: ['**/*.uat.cy.js', '**/*.axe.cy.js'],
  },
  accessibility: {
    specPattern: 'tests/**/*.axe.cy.js',
    excludeSpecPattern: [],
  },
  uat: {
    specPattern: 'tests/**/*.uat.cy.js',
    excludeSpecPattern: [],
  },
  all: {
    specPattern: 'tests/**/*.cy.js',
    excludeSpecPattern: [],
  },
};

const suite = process.env.CYPRESS_SUITE || 'e2e';
const { specPattern, excludeSpecPattern } = SUITE_CONFIG[suite] ?? SUITE_CONFIG.e2e;
const allureResultsPath = process.env.ALLURE_RESULTS_DIR || 'allure-results';

module.exports = defineConfig({
  e2e: {
    baseUrl: testConfig.baseUrl,
    supportFile: 'support/e2e.js',
    specPattern,
    excludeSpecPattern,
    screenshotsFolder: 'cypress/screenshots',
    videosFolder: 'cypress/videos',
    video: false,
    videoCompression: 32,
    screenshotOnRunFailure: true,
    trashAssetsBeforeRuns: Number(process.env.SPLIT || 1) <= 1,
    viewportWidth: 1280,
    viewportHeight: 720,
    defaultCommandTimeout: 10_000,
    requestTimeout: 10_000,
    pageLoadTimeout: 30_000,
    setupNodeEvents(on, config) {
      allureWriter(on, config);
      grep(config);

      on('file:preprocessor', webpackPreprocessor({
        webpackOptions: {
          resolve: {
            alias: {
              '@': path.resolve(__dirname),
            },
          },
        },
      }));

      on('after:spec', (_spec, results) => {
        if (!results?.video) {
          return;
        }

        const hasFailures = results.tests.some((test) =>
          test.attempts.some((attempt) => attempt.state === 'failed'),
        );

        if (!hasFailures) {
          fs.unlinkSync(results.video);
        }
      });

      cypressSplit(on, config);

      return config;
    },
  },
  env: {
    allure: true,
    allureResultsPath,
    grepFilterSpecs: true,
    grepOmitFiltered: true,
    E2E_TEST_ENV_URL: process.env.E2E_TEST_ENV_URL || 'http://localhost:5173',
    E2E_WIREMOCK_URL: process.env.E2E_WIREMOCK_URL || 'http://localhost:8085',
    E2E_API_URL: process.env.E2E_API_URL || 'http://localhost:8080/v1',
    E2E_MONGODB_URL: process.env.E2E_MONGODB_URL || 'mongodb://localhost:27017',
    allureLinkIssuePattern: 'https://github.com/sergii-h/qa-demo/issues/{}',
    allureLinkTmsPattern: 'https://github.com/sergii-h/qa-demo/issues/{}',
  },
});
