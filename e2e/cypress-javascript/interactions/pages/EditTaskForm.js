class EditTaskForm {
  get titleInput() {
    return cy.get('[data-testid="edit-task-title-input"]');
  }

  get descriptionInput() {
    return cy.get('#description');
  }

  get statusDropdown() {
    return cy.get('[data-testid="status-dropdown"]');
  }

  get priorityDropdown() {
    return cy.get('[data-testid="priority-dropdown"]');
  }

  get submitButton() {
    return cy.get('[data-testid="save-button"]');
  }

  statusOption(status) {
    return cy.get(`[data-testid="status-dropdown-option-${status}"]`);
  }

  priorityOption(priority) {
    return cy.get(`[data-testid="priority-dropdown-option-${priority}"]`);
  }
}

module.exports = { EditTaskForm };
