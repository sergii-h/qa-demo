import { TaskPriority } from '@/data/TaskPriority';
import { TaskData } from '@/data/TaskData';
import { TaskRequest } from '@/data/TaskRequest';
import { TaskStatus } from '@/data/TaskStatus';
import { TaskResponse } from '@/data/TaskResponse';

type TaskContextParams = {
  id?: string;
  title?: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
};

export class TaskContext {
  id: string;
  readonly title: string;
  readonly description: string;
  readonly status: TaskStatus;
  readonly priority: TaskPriority;

  constructor(overrides: TaskContextParams = {}) {
    this.id = overrides.id ?? crypto.randomUUID();
    this.title = overrides.title ?? `Task-${crypto.randomUUID().split('-')[0]}`;
    this.description = overrides.description ?? 'Automated test task description';
    this.status = overrides.status ?? TaskStatus.TODO;
    this.priority = overrides.priority ?? TaskPriority.MEDIUM;
  }

  createTaskRequest(): TaskRequest {
    return {
      title: this.title,
      description: this.description,
      status: this.status,
      priority: this.priority,
    };
  }

  createTaskData(): TaskData {
    return {
      title: this.title,
      description: this.description,
      status: this.status,
      priority: this.priority,
    };
  }

  createTaskResponse(): TaskResponse {
    return {
      id: this.id,
      title: this.title,
      description: this.description,
      status: this.status,
      priority: this.priority,
    };
  }
}
