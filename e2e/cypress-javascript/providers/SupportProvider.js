const { ApiClient } = require('@/support/api/ApiClient');
const { WireMockClient } = require('@/support/mocks/WireMockClient');
const { ApiRouteMock } = require('@/support/mocks/ApiRouteMock');

class SupportProvider {
  constructor() {
    this.api = new ApiClient();
    this.wiremock = new WireMockClient();
    this.mock = { api: new ApiRouteMock() };
  }
}

module.exports = { SupportProvider };
