const SOFT_GREEN = 'eaf7ee';
const SOFT_GREEN_DARK = '166534';

function greenBadge(label, logo, logoColor) {
  const encodedLabel = encodeURIComponent(label);
  let url = `https://img.shields.io/badge/${encodedLabel}-${SOFT_GREEN_DARK}?color=${SOFT_GREEN}&labelColor=${SOFT_GREEN}`;
  if (logo) {
    url += `&logo=${logo}&logoColor=${logoColor}`;
  }
  return url;
}

const PLAYWRIGHT_LOGO = './assets/playwright-logo.png';
const SELENIDE_LOGO = './assets/selenide-logo.png';
const ALLURE_LOGO = './assets/allure-logo.png';
const MAESTRO_LOGO = './assets/maestro-logo.png';
const REACT_NATIVE_LOGO = './assets/react-native-logo.png';
const IOS_LOGO = './assets/ios-logo.png';

const LOCAL_LOGO_ASSETS = {
  'maestro-logo': { className: 'logo-static--maestro', alt: 'Maestro' },
  'react-native-logo': { className: 'logo-static--react-native', alt: 'React Native' },
  'ios-logo': { className: 'logo-static--ios', alt: 'iOS' },
};

const BRAND_CONFIG = {
  playwright: { logo: PLAYWRIGHT_LOGO, className: 'brand-chip--playwright' },
  selenide: { logo: SELENIDE_LOGO, className: 'brand-chip--selenide' },
  allure: { logo: ALLURE_LOGO, className: 'brand-chip--allure' },
};

const PLAYWRIGHT_BRAND = { kind: 'playwright', label: 'Playwright' };
const SELENIDE_BRAND = { kind: 'selenide', label: 'Selenide' };
const ALLURE_REPORT_BRAND = { kind: 'allure', label: 'Allure report' };

const BADGES = {
  java21: 'https://img.shields.io/badge/Java-21-orange?logo=openjdk',
  seleniumGrid: greenBadge('Selenium Grid', 'selenium', '43B02A'),
  junit5: greenBadge('JUnit5', 'junit5', '25A162'),
  typescript5: 'https://img.shields.io/badge/TypeScript-5-blue?logo=typescript',
  python312: 'https://img.shields.io/badge/Python-3.12-blue?logo=python',
  kotlin: 'https://img.shields.io/badge/Kotlin-purple?logo=kotlin',
  android: greenBadge('Android', 'android', '3DDC84'),
  compose: 'https://img.shields.io/badge/Compose-blue?logo=jetpackcompose',
};

const PAGE = {
  master: {
    title: 'QA Demo — Test Reports',
    intro: 'Latest reports published from CI on the <code>master</code> branch.',
  },
  pr: {
    title: 'QA Demo — Pull Request Test Reports',
    intro: 'Test reports for this pull request, published from CI.',
  },
};

const TEST_RUNS = [
  {
    titleEmphasis: 'Selenide',
    titleSuffix: 'test-run',
    subtitle: 'Web · Desktop & mobile Chrome',
    headerLogos: [
      BADGES.java21,
      SELENIDE_BRAND,
      BADGES.seleniumGrid,
      BADGES.junit5,
    ],
    suites: [
      { suite: 'E2E', meta: 'mocked backend', allure: './selenide-e2e/index.html' },
      { suite: 'Accessibility', meta: 'mocked backend', allure: './selenide-accessibility/index.html' },
      { suite: 'UAT', meta: 'full stack', allure: './selenide-uat/index.html' },
    ],
  },
  {
    titleEmphasis: 'Playwright',
    titleSuffix: 'test-run (TypeScript)',
    subtitle: 'Web · Desktop Chrome & Mobile Safari (webkit)',
    headerLogos: [
      BADGES.typescript5,
      PLAYWRIGHT_BRAND,
    ],
    suites: [
      {
        suite: 'E2E',
        meta: 'mocked backend',
        allure: './playwright-e2e/index.html',
        playwrightHtml: './playwright-html-e2e/index.html',
      },
      {
        suite: 'Accessibility',
        meta: 'mocked backend',
        allure: './playwright-accessibility/index.html',
        playwrightHtml: './playwright-html-accessibility/index.html',
      },
      {
        suite: 'UAT',
        meta: 'full stack',
        allure: './playwright-uat/index.html',
        playwrightHtml: './playwright-html-uat/index.html',
      },
    ],
  },
  {
    titleEmphasis: 'Cypress',
    titleSuffix: 'test-run (JavaScript)',
    subtitle: 'Web · Desktop Chrome',
    headerLogos: [
      'https://img.shields.io/badge/JavaScript-ES2022-yellow?logo=javascript',
      greenBadge('Cypress', 'cypress', '17202C'),
    ],
    suites: [
      { suite: 'E2E', meta: 'mocked backend', allure: './cypress-e2e/index.html' },
      { suite: 'Accessibility', meta: 'mocked backend', allure: './cypress-accessibility/index.html' },
      { suite: 'UAT', meta: 'full stack', allure: './cypress-uat/index.html' },
    ],
  },
  {
    titleEmphasis: 'Playwright',
    titleSuffix: 'test-run (Python)',
    subtitle: 'Web · Desktop Chrome & Mobile Safari (webkit)',
    headerLogos: [
      BADGES.python312,
      PLAYWRIGHT_BRAND,
    ],
    suites: [
      { suite: 'E2E', meta: 'mocked backend', allure: './playwright-python-e2e/index.html' },
      { suite: 'Accessibility', meta: 'mocked backend', allure: './playwright-python-accessibility/index.html' },
      { suite: 'UAT', meta: 'full stack', allure: './playwright-python-uat/index.html' },
    ],
  },
  {
    titleEmphasis: 'Android Compose',
    titleSuffix: 'test-run',
    subtitle: 'Native Android app',
    headerLogos: [
      BADGES.kotlin,
      BADGES.android,
      BADGES.compose,
    ],
    suites: [
      { suite: 'E2E', meta: 'mocked backend', allure: './android-compose-e2e/index.html' },
      { suite: 'Accessibility', meta: 'mocked backend', allure: './android-compose-accessibility/index.html' },
      { suite: 'UAT', meta: 'full stack', allure: './android-compose-uat/index.html' },
    ],
  },
  {
    titleEmphasis: 'Maestro React Native',
    titleSuffix: 'test-run (Android)',
    subtitle: 'React Native app · release APK · Android emulator',
    headerLogos: [
      REACT_NATIVE_LOGO,
      MAESTRO_LOGO,
      BADGES.android,
    ],
    suites: [
      { suite: 'E2E', meta: 'mocked backend', allure: './maestro-android-e2e/index.html' },
      { suite: 'Accessibility', meta: 'mocked backend', allure: './maestro-android-accessibility/index.html' },
      { suite: 'UAT', meta: 'full stack', allure: './maestro-android-uat/index.html' },
    ],
  },
  {
    titleEmphasis: 'Maestro React Native',
    titleSuffix: 'test-run (iOS)',
    subtitle: 'React Native app · release simulator build · iOS Simulator',
    headerLogos: [
      REACT_NATIVE_LOGO,
      MAESTRO_LOGO,
      IOS_LOGO,
    ],
    suites: [
      { suite: 'E2E', meta: 'mocked backend', allure: './maestro-ios-e2e/index.html' },
      { suite: 'Accessibility', meta: 'mocked backend', allure: './maestro-ios-accessibility/index.html' },
      {
        suite: 'UAT',
        meta: 'full stack · not published in CI (Docker unavailable on GitHub macOS runners)',
        allureDisabled: true,
      },
    ],
  },
];

function isPullRequestPage() {
  return /\/pr\/\d+(?:\/|$)/.test(window.location.pathname);
}

function isBrandChip(item) {
  return item && typeof item === 'object' && BRAND_CONFIG[item.kind];
}

function renderBrandChip(item, compact = false) {
  const brand = BRAND_CONFIG[item.kind];
  const compactClass = compact ? ' brand-chip--compact' : '';
  return `
    <span class="brand-chip ${brand.className}${compactClass}">
      <img class="logo-static" src="${brand.logo}" alt="${item.label}">
      <span class="brand-chip__label">${item.label}</span>
    </span>
  `;
}

function localAssetMeta(src) {
  const entry = Object.entries(LOCAL_LOGO_ASSETS).find(([name]) => src.includes(name));
  return entry ? entry[1] : null;
}

function renderLogo(item, alt = '') {
  if (isBrandChip(item)) {
    return renderBrandChip(item);
  }
  if (typeof item === 'string' && item.startsWith('./assets/')) {
    const meta = localAssetMeta(item);
    const assetClass = meta ? ` ${meta.className}` : '';
    const altText = alt || meta?.alt || '';
    return `<img class="logo-static${assetClass}" src="${item}" alt="${altText}">`;
  }
  return `<img src="${item}" alt="${alt}">`;
}

function badgeImages(sources) {
  return sources.map((item) => renderLogo(item)).join('');
}

function renderAllureReportAction(href) {
  return `
    <a class="report-action report-action--chip" href="${href}" title="Open Allure report">
      ${renderBrandChip(ALLURE_REPORT_BRAND, true)}
    </a>
  `;
}

function renderDisabledAllureReportAction() {
  return `
    <span class="report-action report-action--chip report-action--disabled" aria-disabled="true" title="Report not available">
      ${renderBrandChip(ALLURE_REPORT_BRAND, true)}
    </span>
  `;
}

function renderSuiteRow(suite) {
  const playwrightHtmlAction = suite.playwrightHtml
    ? `
      <a class="report-action report-action--chip" href="${suite.playwrightHtml}" title="Open Playwright HTML report">
        ${renderBrandChip({ kind: 'playwright', label: 'Playwright report' }, true)}
      </a>
    `
    : '';

  return `
    <li>
      <div class="suite-row">
        <span class="label">
          <span class="suite">${suite.suite}</span>
          <span class="meta">${suite.meta}</span>
        </span>
        <div class="report-actions">
          ${suite.allureDisabled ? renderDisabledAllureReportAction() : renderAllureReportAction(suite.allure)}
          ${playwrightHtmlAction}
        </div>
      </div>
    </li>
  `;
}

function renderTestRunTitle(testRun) {
  return `<span class="test-run-title-emphasis">${testRun.titleEmphasis}</span> <span class="test-run-title-suffix">${testRun.titleSuffix}</span>`;
}

function renderTestRun(testRun) {
  return `
    <section class="test-run">
      <div class="test-run-header">
        <h2 class="test-run-title test-run-title--split">${renderTestRunTitle(testRun)}</h2>
        <div class="test-run-logos">${badgeImages(testRun.headerLogos)}</div>
        <p class="test-run-subtitle">${testRun.subtitle}</p>
      </div>
      <ul class="suite-list">
        ${testRun.suites.map(renderSuiteRow).join('')}
      </ul>
    </section>
  `;
}

function renderPage() {
  const page = isPullRequestPage() ? PAGE.pr : PAGE.master;
  document.title = page.title;

  document.getElementById('app').innerHTML = `
    <header class="page-header">
      <h1>QA Demo E2E Test Reports</h1>
      <p class="intro">${page.intro}</p>
    </header>

    ${TEST_RUNS.map(renderTestRun).join('')}

    <footer>Reports are published together after web, Android Compose, and Maestro React Native E2E suites finish in CI.</footer>
  `;
}

renderPage();
