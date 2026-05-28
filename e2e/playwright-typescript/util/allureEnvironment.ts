import { chromium, webkit, type BrowserType } from '@playwright/test';

const browsers: Array<[string, BrowserType]> = [
  ['Chromium', chromium],
  ['WebKit', webkit],
];

export async function resolvePlaywrightBrowserVersions(): Promise<string> {
  const versions: string[] = [];

  for (const [label, browserType] of browsers) {
    try {
      const browser = await browserType.launch({ headless: true });
      versions.push(`${label} ${browser.version()}`);
      await browser.close();
    } catch {
      // Browser is not installed for this run.
    }
  }

  return versions.length > 0 ? versions.join(', ') : 'unknown';
}
