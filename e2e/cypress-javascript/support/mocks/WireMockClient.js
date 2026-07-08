const testConfig = require('@/test.config');

class WireMockClient {
  constructor() {
    this.baseUrl = testConfig.wiremock.url;
  }

  clearMocks() {
    cy.request('DELETE', `${this.baseUrl}/__admin/mappings`);
    return this;
  }

  stubTaskValidation(isValid) {
    cy.request('POST', `${this.baseUrl}/__admin/mappings`, {
      request: {
        method: 'GET',
        urlPattern: '/api/tasks/.*/validation',
      },
      response: {
        status: 200,
        jsonBody: { valid: isValid },
      },
    });
    return this;
  }
}

module.exports = { WireMockClient };
