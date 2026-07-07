function env(key, fallback) {
  if (typeof Cypress !== 'undefined' && Cypress.env) {
    const value = Cypress.env(key);
    if (value !== undefined && value !== '') {
      return value;
    }
  }

  if (typeof process !== 'undefined' && process.env) {
    const value = process.env[key];
    if (value !== undefined && value !== '') {
      return value;
    }
  }

  return fallback;
}

module.exports = {
  baseUrl: env('E2E_TEST_ENV_URL', 'http://localhost:5173'),
  wiremock: {
    url: env('E2E_WIREMOCK_URL', 'http://localhost:8085'),
  },
  services: {
    api: {
      url: env('E2E_API_URL', 'http://localhost:8080/v1'),
    },
  },
  mongodb: {
    devHfa: env('E2E_MONGODB_URL', 'mongodb://localhost:27017'),
  },
};
