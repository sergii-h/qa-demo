import path from 'node:path';

import dotenv from 'dotenv';

import { MaestroTestRunner } from '@/runner/MaestroTestRunner';
import type { TestSuite } from '@/runner/types';

dotenv.config({ path: path.resolve(__dirname, '.env.e2e') });

const resultsDir =
  process.env.ALLURE_RESULTS_DIR ?? path.resolve(__dirname, 'allure-results');

const maestroOutputRoot =
  process.env.MAESTRO_OUTPUT_DIR ?? path.resolve(__dirname, '.maestro-output');

const suite = process.argv[2] as TestSuite;

if (!['mocked', 'uat', 'accessibility'].includes(suite)) {
  console.error('Usage: tsx runSuite.ts <mocked|uat|accessibility>');
  process.exit(1);
}

const runner = new MaestroTestRunner(__dirname, resultsDir, maestroOutputRoot);

runner.run(suite).catch((error) => {
  console.error(error);
  process.exit(1);
});
