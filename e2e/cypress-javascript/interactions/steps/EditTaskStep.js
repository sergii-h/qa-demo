const { step } = require('@/decorators/step-decorator');
const { EditTaskForm } = require('@/interactions/pages/EditTaskForm');

class EditTaskStep {
  constructor() {
    this.form = new EditTaskForm();
    this.fillForm = step('Fill edit task form with {taskData}', this._fillForm.bind(this));
    this.submitForm = step('Submit edit task form', this._submitForm.bind(this));
  }

  _fillForm(taskData) {
    if (taskData.title) {
      this.form.titleInput.clear().type(taskData.title);
    }
    if (taskData.description) {
      this.form.descriptionInput.clear().type(taskData.description);
    }
    if (taskData.status) {
      this.form.statusDropdown.click();
      this.form.statusOption(taskData.status).click();
    }
    if (taskData.priority) {
      this.form.priorityDropdown.click();
      this.form.priorityOption(taskData.priority).click();
    }
    return this;
  }

  _submitForm() {
    this.form.submitButton.click();
    this.form.submitButton.should('not.exist');
    return this;
  }
}

module.exports = { EditTaskStep };
