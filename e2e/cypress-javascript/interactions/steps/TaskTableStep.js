const { step } = require('@/decorators/step-decorator');
const { MainPage } = require('@/interactions/pages/MainPage');
const { CreateTaskForm } = require('@/interactions/pages/CreateTaskForm');
const { CreateTaskStep } = require('@/interactions/steps/CreateTaskStep');
const { EditTaskStep } = require('@/interactions/steps/EditTaskStep');
const { TaskInfoModal } = require('@/interactions/pages/TaskInfoModal');
const { EditTaskForm } = require('@/interactions/pages/EditTaskForm');

class TaskTableStep {
  constructor() {
    this.mainPage = new MainPage();
    this.createTaskForm = new CreateTaskForm();
    this.taskInfoModal = new TaskInfoModal();
    this.editTaskForm = new EditTaskForm();
    this.createTask = new CreateTaskStep();
    this.editTask = new EditTaskStep();

    this.openCreateTaskForm = step('Open create task form', this._openCreateTaskForm.bind(this));
    this.openTaskInfoForm = step("Open task info for task '{title}'", this._openTaskInfoForm.bind(this));
    this.openEditTaskForm = step("Open edit form for task '{title}'", this._openEditTaskForm.bind(this));
    this.deleteTask = step("Delete task '{title}'", this._deleteTask.bind(this));
  }

  _openCreateTaskForm() {
    this.mainPage.createTaskButton.click();
    this.createTaskForm.submitButton.should('be.visible');
    return this.createTask;
  }

  _openTaskInfoForm(title) {
    this._resolveTaskId(title).then((id) => {
      this.mainPage.taskInfoButtonById(id).click();
      this.taskInfoModal.title.should('be.visible');
    });
  }

  _openEditTaskForm(title) {
    this._resolveTaskId(title).then((id) => {
      this.mainPage.taskEditButtonById(id).click();
      this.editTaskForm.titleInput.should('have.value', title);
    });
    return this.editTask;
  }

  _deleteTask(title) {
    this._resolveTaskId(title).then((id) => {
      this.mainPage.taskDeleteButtonById(id).click();
      this.mainPage.taskDeleteButtonById(id).should('not.exist');
    });
  }

  _resolveTaskId(title) {
    return this.mainPage.taskTitleByTitle(title)
      .invoke('attr', 'data-testid')
      .then((dataTestId) => dataTestId.replace('task-title-', ''));
  }
}

module.exports = { TaskTableStep };
