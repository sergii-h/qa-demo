import { defineConfig, devices } from '@playwright/test';
import dotenv from 'dotenv';
import * as fs from 'node:fs';
import * as os from 'node:os';
import * as path from 'node:path';
import { testConfig } from './test.config';

const envPath = fs.existsSync(path.resolve(__dirname, '.env.e2e.local'))
  ? path.resolve(__dirname, '.env.e2e.local')
  : path.resolve(__dirname, '.env.e2e');

dotenv.config({ path: envPath });

export default defineConfig({
  testDir: './',
  timeout: 30_000,
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: 0,

  reporter: [
    ['html'],
    [
      'allure-playwright',
      {
        resultsDir: 'allure-results',
        detail: true,
        suiteTitle: true,
        environmentInfo: {
          Framework: 'Playwright',
          os_release: os.release(),
          os_version: os.version(),
          node_version: process.version,
          environment: process.env.E2E_TEST_ENV_URL,
        },
        links: {
          issue: {
            nameTemplate: "Issue #%s",
            urlTemplate: "https://github.com/sergii-h/qa-demo/issues/%s",
          },
          tms: {
            nameTemplate: "TMS #%s",
            urlTemplate: "https://github.com/sergii-h/qa-demo/issues/%s",
          },
        },
      },
    ],
  ],

  expect: {
    timeout: 10_000,
  },

  use: {
    baseURL: testConfig.baseUrl,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },

  projects: [
    {
      name: 'Desktop Chrome (chromium)',
      use: {
        ...devices['Desktop Chrome'],
        isMobile: false,
        launchOptions: {
          args: ['--disable-dev-shm-usage'],
        },
      },
    },
    {
      name: 'Mobile Safari (webkit)',
      use: {
        ...devices['iPhone 12 Pro'],
        isMobile: true,
      },
    },
  ],
});
