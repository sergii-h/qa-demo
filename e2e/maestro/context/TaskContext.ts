import { TaskPriority } from '@/data/TaskPriority';
import { TaskData } from '@/data/TaskData';
import { TaskResponse } from '@/data/TaskResponse';
import { TaskStatus } from '@/data/TaskStatus';

type TaskContextParams = {
  id?: string;
  title?: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
};

export class TaskContext {
  readonly id: string;
  readonly title: string;
  readonly description: string;
  readonly status: TaskStatus;
  readonly priority: TaskPriority;

  constructor(overrides: TaskContextParams = {}) {
    this.id = overrides.id ?? crypto.randomUUID();
    this.title = overrides.title ?? randomAlphabetic(12);
    this.description = overrides.description ?? randomAlphabetic(12);
    this.status = overrides.status ?? TaskStatus.TODO;
    this.priority = overrides.priority ?? TaskPriority.MEDIUM;
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

  withUpdates(overrides: TaskContextParams): TaskContext {
    return new TaskContext({
      id: this.id,
      title: overrides.title ?? this.title,
      description: overrides.description ?? this.description,
      status: overrides.status ?? this.status,
      priority: overrides.priority ?? this.priority,
    });
  }

  toMaestroEnv(prefix = ''): Record<string, string> {
    const key = (name: string) => (prefix ? `${prefix}_${name}` : name);
    return {
      [key('TASK_ID')]: this.id,
      [key('TASK_TITLE')]: this.title,
      [key('TASK_DESCRIPTION')]: this.description,
      [key('TASK_STATUS')]: this.status,
      [key('TASK_PRIORITY')]: this.priority,
    };
  }
}

function randomAlphabetic(length: number): string {
  return Array.from({ length }, () =>
    String.fromCharCode(65 + Math.floor(Math.random() * 26)),
  ).join('');
}
