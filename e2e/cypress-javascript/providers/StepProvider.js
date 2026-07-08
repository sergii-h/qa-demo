const { AccessibilityStep } = require('@/interactions/steps/AccessibilityStep');
const { CreateTaskStep } = require('@/interactions/steps/CreateTaskStep');
const { EditTaskStep } = require('@/interactions/steps/EditTaskStep');
const { LanguageSwitcherStep } = require('@/interactions/steps/LanguageSwitcherStep');
const { NavigationStep } = require('@/interactions/steps/NavigationStep');
const { TaskTableStep } = require('@/interactions/steps/TaskTableStep');

class StepProvider {
  constructor() {
    this.navigation = new NavigationStep();
    this.tasks = new TaskTableStep();
    this.createTask = new CreateTaskStep();
    this.editTask = new EditTaskStep();
    this.language = new LanguageSwitcherStep();
    this.accessibility = new AccessibilityStep();
  }
}

module.exports = { StepProvider };
