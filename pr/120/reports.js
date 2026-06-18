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
    titleSuffix: 'test-run',
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

function renderLogo(item, alt = '') {
  if (isBrandChip(item)) {
    return renderBrandChip(item);
  }
  if (typeof item === 'string' && item.startsWith('./assets/')) {
    return `<img class="logo-static" src="${item}" alt="${alt}">`;
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
          ${renderAllureReportAction(suite.allure)}
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

    <footer>Reports are published together after all E2E suites finish in CI.</footer>
  `;
}

renderPage();
