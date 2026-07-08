import { defineConfig, devices } from '@playwright/test';
import { execFileSync } from 'node:child_process';
import dotenv from 'dotenv';
import * as fs from 'node:fs';
import * as os from 'node:os';
import * as path from 'node:path';
import { testConfig } from './test.config';

const envPath = fs.existsSync(path.resolve(__dirname, '.env.e2e.local'))
  ? path.resolve(__dirname, '.env.e2e.local')
  : path.resolve(__dirname, '.env.e2e');

dotenv.config({ path: envPath });

const CONFIGURED_BROWSERS = ['chromium', 'webkit'];
const BROWSER_LINE = /^(.+?)\s+\(playwright (chromium|webkit|firefox)(?:-headless-shell)? v\d+\)/;

const resolveBrowserVersionsFromCli = (browsers: string[]): Record<string, string> => {
  const output = execFileSync('npx', ['playwright', 'install', '--dry-run', ...browsers], {
    cwd: __dirname,
    encoding: 'utf8',
  });

  const versions: Record<string, string> = {};

  for (const line of output.split('\n')) {
    const match = line.trim().match(BROWSER_LINE);
    if (!match) {
      continue;
    }

    const [, label, browserName] = match;
    if (browserName.includes('headless-shell') || versions[browserName]) {
      continue;
    }

    versions[browserName] = label.match(/(\d+(?:\.\d+)*)/)?.[1] ?? label;
  }

  return versions;
};

const browserVersions = resolveBrowserVersionsFromCli(CONFIGURED_BROWSERS);

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
          browser: Object.keys(browserVersions).join(', '),
          browser_version: Object.entries(browserVersions)
            .map(([name, version]) => `${name} ${version}`)
            .join('; '),
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
