const testConfig = require('@/test.config');

class ApiClient {
  constructor() {
    this.baseUrl = testConfig.services.api.url;
  }

  createTask(task) {
    return cy.request('POST', `${this.baseUrl}/tasks`, task).its('body');
  }

  deleteTask(id) {
    return cy.request('DELETE', `${this.baseUrl}/tasks/${id}`);
  }

  getTasks() {
    return cy.request('GET', `${this.baseUrl}/tasks`).its('body');
  }
}

module.exports = { ApiClient };
