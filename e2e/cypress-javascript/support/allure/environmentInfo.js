const fs = require('fs');
const os = require('os');
const path = require('path');

const { DEVICES } = require('../../data/devices');

const buildAllureEnvironmentInfo = (browser = {}, device = {}) => ({
  Framework: 'Cypress',
  os_release: os.release(),
  os_version: os.version(),
  node_version: process.version,
  environment: process.env.E2E_TEST_ENV_URL || 'http://localhost:5173',
  device: device.label || 'desktop',
  browser: browser.displayName || browser.name || 'unknown',
  browser_version: browser.version || browser.majorVersion || 'unknown',
});

const writeAllureEnvironmentInfo = (resultsDir, browser = {}, device = {}) => {
  const lines = Object.entries(buildAllureEnvironmentInfo(browser, device))
    .map(([key, value]) => `${key}=${value}`)
    .join('\n');

  fs.mkdirSync(resultsDir, { recursive: true });
  fs.writeFileSync(path.join(resultsDir, 'environment.properties'), `${lines}\n`);
};

const resolveDeviceLabelFromDir = (sourceDir) => {
  if (sourceDir.includes(`${path.sep}.device-desktop`) || sourceDir.endsWith('.device-desktop')) {
    const { label, viewportWidth, viewportHeight } = DEVICES.desktop;
    return `${label} (${viewportWidth}x${viewportHeight})`;
  }

  if (sourceDir.includes(`${path.sep}.device-mobile`) || sourceDir.endsWith('.device-mobile')) {
    const { label, viewportWidth, viewportHeight } = DEVICES.mobile;
    return `${label} (${viewportWidth}x${viewportHeight})`;
  }

  return null;
};

const readProperty = (content, key) => content.match(new RegExp(`^${key}=(.+)$`, 'm'))?.[1];

const writeMergedAllureEnvironmentInfo = (targetDir, sourceDirs) => {
  let browser = 'Chrome';
  let browserVersion = 'unknown';

  for (const sourceDir of sourceDirs) {
    const envFile = path.join(sourceDir, 'environment.properties');
    if (!fs.existsSync(envFile)) {
      continue;
    }

    const content = fs.readFileSync(envFile, 'utf8');
    browser = readProperty(content, 'browser') || browser;
    browserVersion = readProperty(content, 'browser_version') || browserVersion;
    break;
  }

  const device = sourceDirs
    .map(resolveDeviceLabelFromDir)
    .filter(Boolean)
    .join('; ') || DEVICES.desktop.label;

  const lines = Object.entries({
    Framework: 'Cypress',
    os_release: os.release(),
    os_version: os.version(),
    node_version: process.version,
    environment: process.env.E2E_TEST_ENV_URL || 'http://localhost:5173',
    device,
    browser,
    browser_version: browserVersion,
  })
    .map(([key, value]) => `${key}=${value}`)
    .join('\n');

  fs.mkdirSync(targetDir, { recursive: true });
  fs.writeFileSync(path.join(targetDir, 'environment.properties'), `${lines}\n`);
};

module.exports = {
  buildAllureEnvironmentInfo,
  writeAllureEnvironmentInfo,
  writeMergedAllureEnvironmentInfo,
};
