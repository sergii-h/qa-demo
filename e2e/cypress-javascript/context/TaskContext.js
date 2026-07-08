const { TaskPriority } = require('@/data/TaskPriority');
const { TaskStatus } = require('@/data/TaskStatus');

class TaskContext {
  constructor(overrides = {}) {
    this.id = overrides.id ?? crypto.randomUUID();
    this.title = overrides.title ?? `Task-${crypto.randomUUID().split('-')[0]}`;
    this.description = overrides.description ?? 'Automated test task description';
    this.status = overrides.status ?? TaskStatus.TODO;
    this.priority = overrides.priority ?? TaskPriority.MEDIUM;
  }

  createTaskRequest() {
    return {
      title: this.title,
      description: this.description,
      status: this.status,
      priority: this.priority,
    };
  }

  createTaskData() {
    return {
      title: this.title,
      description: this.description,
      status: this.status,
      priority: this.priority,
    };
  }

  createTaskResponse() {
    return {
      id: this.id,
      title: this.title,
      description: this.description,
      status: this.status,
      priority: this.priority,
    };
  }
}

module.exports = { TaskContext };
