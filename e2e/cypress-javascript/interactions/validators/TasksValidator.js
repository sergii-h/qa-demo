const { step } = require('@/decorators/step-decorator');
const { MainPage } = require('@/interactions/pages/MainPage');

class TasksValidator {
  constructor() {
    this.mainPage = new MainPage();
    this.hasTask = step('Validate task with title {title} is visible', this._hasTask.bind(this));
    this.hasNoTask = step('Validate task with title {title} is not visible', this._hasNoTask.bind(this));
    this.hasTaskCount = step('Validate task count is {count}', this._hasTaskCount.bind(this));
  }

  _hasTask(title) {
    this.mainPage.taskRowByTitle(title).should('be.visible');
    return this;
  }

  _hasNoTask(title) {
    this.mainPage.taskTableBody.should('not.contain', title);
    return this;
  }

  _hasTaskCount(count) {
    if (count === 0) {
      this.mainPage.taskRows.should('not.exist');
    } else {
      this.mainPage.taskRows.should('have.length', count);
    }
    return this;
  }
}

module.exports = { TasksValidator };
