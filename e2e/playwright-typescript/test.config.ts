export const testConfig = {
  baseUrl: process.env.E2E_TEST_ENV_URL || 'http://localhost:5173',
  wiremock: {
    url: process.env.E2E_WIREMOCK_URL || 'http://localhost:8085',
  },
  services: {
    api: {
      url: process.env.E2E_API_URL || 'http://localhost:8080/v1',
    },
  },
  mongodb: {
    devHfa: process.env.E2E_MONGODB_URL || 'mongodb://localhost:27017',
  },
};
