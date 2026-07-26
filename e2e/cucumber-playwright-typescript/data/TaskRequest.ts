import { TaskPriority } from './TaskPriority';
import { TaskStatus } from './TaskStatus';

export type TaskRequest = {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
};
