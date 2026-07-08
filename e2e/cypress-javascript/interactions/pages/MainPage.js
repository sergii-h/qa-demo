class MainPage {
  get taskRows() {
    return cy.get('[data-testid^="task-title-"]');
  }

  get createTaskButton() {
    return cy.get('[data-testid="add-task-button"]');
  }

  get languageSwitcher() {
    return cy.get('[data-testid="language-switcher"]');
  }

  get tableHeaders() {
    return cy.get('.p-datatable-thead');
  }

  get taskTableBody() {
    return cy.get('.p-datatable-tbody');
  }

  taskRowByTitle(title) {
    return cy.get('[data-testid^="task-title-"]').contains(title);
  }

  taskInfoButtonById(id) {
    return cy.get(`[data-testid="info-button-${id}"]`);
  }

  taskEditButtonById(id) {
    return cy.get(`[data-testid="edit-button-${id}"]`);
  }

  taskDeleteButtonById(id) {
    return cy.get(`[data-testid="delete-button-${id}"]`);
  }

  taskTitleByTitle(title) {
    return cy.get('[data-testid^="task-title-"]').contains(title);
  }

  statusTag(status) {
    return cy.get(`[data-testid="status-tag-${status}"]`).first();
  }

  priorityTag(priority) {
    return cy.get(`[data-testid="priority-tag-${priority}"]`).first();
  }
}

module.exports = { MainPage };
