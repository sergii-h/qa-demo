class ApiRouteMock {
  createTask(response) {
    cy.intercept('POST', '**/v1/tasks', { statusCode: 201, body: response });
    return this;
  }

  getTasks(response) {
    cy.intercept('GET', '**/v1/tasks', { statusCode: 200, body: response });
    return this;
  }

  getTask(id, response) {
    cy.intercept('GET', `**/v1/tasks/${id}`, { statusCode: 200, body: response });
    return this;
  }

  deleteTask(id) {
    cy.intercept('DELETE', `**/v1/tasks/${id}`, { statusCode: 204 });
    return this;
  }

  updateTask(id, response) {
    cy.intercept('PUT', `**/v1/tasks/${id}`, { statusCode: 200, body: response });
    return this;
  }

  getIsValid(id, isValid) {
    cy.intercept('GET', `**/v1/tasks/isValid/${id}`, { statusCode: 200, body: isValid });
    return this;
  }
}

module.exports = { ApiRouteMock };
