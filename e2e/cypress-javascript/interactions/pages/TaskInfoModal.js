class TaskInfoModal {
  get title() {
    return cy.get('[data-testid="modal-title"]');
  }

  get description() {
    return cy.get('[data-testid="description"]');
  }

  get validationBadge() {
    return cy.get('[data-testid="valid"]');
  }

  statusTag(status) {
    return cy.get('[data-testid="status"]').find(`[data-testid="status-tag-${status}"]`);
  }

  priorityTag(priority) {
    return cy.get('[data-testid="priority"]').find(`[data-testid="priority-tag-${priority}"]`);
  }
}

module.exports = { TaskInfoModal };
