export const testConfig = {
  wiremock: {
    url: process.env.E2E_WIREMOCK_URL || 'http://localhost:8085',
  },
  api: {
    url: process.env.E2E_API_URL || 'http://localhost:8080/v1',
  },
  maestro: {
    appId: process.env.MAESTRO_APP_ID || 'com.example.demo',
  },
};
