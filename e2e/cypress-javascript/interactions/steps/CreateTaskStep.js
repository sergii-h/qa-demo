const { step } = require('@/decorators/step-decorator');
const { CreateTaskForm } = require('@/interactions/pages/CreateTaskForm');

class CreateTaskStep {
  constructor() {
    this.form = new CreateTaskForm();
    this.fillForm = step('Fill create task form with {taskData}', this._fillForm.bind(this));
    this.submitForm = step('Submit create task form', this._submitForm.bind(this));
  }

  _fillForm(taskData) {
    this.form.titleInput.clear().type(taskData.title);
    this.form.descriptionInput.clear().type(taskData.description);
    this.form.statusDropdown.click();
    this.form.statusOption(taskData.status).click();
    this.form.priorityDropdown.click();
    this.form.priorityOption(taskData.priority).click();
    return this;
  }

  _submitForm() {
    this.form.submitButton.click();
    this.form.submitButton.should('not.exist');
    return this;
  }
}

module.exports = { CreateTaskStep };
