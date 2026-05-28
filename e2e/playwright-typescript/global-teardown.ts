import * as fs from 'node:fs';
import * as path from 'node:path';
import { resolvePlaywrightBrowserVersions } from './util/allureEnvironment';

export default async function globalTeardown() {
  const resultsDir = path.resolve(__dirname, 'allure-results');
  const envFile = path.join(resultsDir, 'environment.properties');
  const browser = await resolvePlaywrightBrowserVersions();

  fs.mkdirSync(resultsDir, { recursive: true });

  const lines = fs.existsSync(envFile)
    ? fs.readFileSync(envFile, 'utf8').split('\n').filter(line => line && !line.startsWith('browser='))
    : [];

  lines.push(`browser=${browser}`);
  fs.writeFileSync(envFile, `${lines.join('\n')}\n`);
}
