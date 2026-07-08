const { AccessibilityValidator } = require('@/interactions/validators/AccessibilityValidator');
const { LanguageValidator } = require('@/interactions/validators/LanguageValidator');
const { TaskValidator } = require('@/interactions/validators/TaskValidator');
const { TasksValidator } = require('@/interactions/validators/TasksValidator');

class ValidationProvider {
  constructor() {
    this.tasks = new TasksValidator();
    this.task = new TaskValidator();
    this.language = new LanguageValidator();
    this.accessibility = new AccessibilityValidator();
  }
}

module.exports = { ValidationProvider };
