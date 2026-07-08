const fs = require('fs');
const os = require('os');
const path = require('path');

const buildAllureEnvironmentInfo = () => ({
  Framework: 'Cypress',
  os_release: os.release(),
  os_version: os.version(),
  node_version: process.version,
  environment: process.env.E2E_TEST_ENV_URL || 'http://localhost:5173',
});

const writeAllureEnvironmentInfo = (resultsDir) => {
  const lines = Object.entries(buildAllureEnvironmentInfo())
    .map(([key, value]) => `${key}=${value}`)
    .join('\n');

  fs.mkdirSync(resultsDir, { recursive: true });
  fs.writeFileSync(path.join(resultsDir, 'environment.properties'), `${lines}\n`);
};

module.exports = { buildAllureEnvironmentInfo, writeAllureEnvironmentInfo };
