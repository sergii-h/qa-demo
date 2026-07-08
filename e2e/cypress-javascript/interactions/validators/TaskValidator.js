const { step } = require('@/decorators/step-decorator');
const { TaskInfoModal } = require('@/interactions/pages/TaskInfoModal');

class TaskValidator {
  constructor() {
    this.modal = new TaskInfoModal();
    this.data = step('Validate task info data: {taskData}', this._data.bind(this));
    this.isValid = step('Validate task is marked as valid', this._isValid.bind(this));
    this.isNotValid = step('Validate task is marked as not valid', this._isNotValid.bind(this));
  }

  _data(task) {
    this.modal.title.should('have.text', task.title);
    this.modal.description.should('have.text', task.description);
    this.modal.statusTag(task.status).should('be.visible');
    this.modal.priorityTag(task.priority).should('be.visible');
    return this;
  }

  _isValid() {
    this.modal.validationBadge.should('be.visible');
    return this;
  }

  _isNotValid() {
    this.modal.validationBadge.should('not.exist');
    return this;
  }
}

module.exports = { TaskValidator };
