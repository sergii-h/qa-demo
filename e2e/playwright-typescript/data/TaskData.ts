import { TaskPriority } from './TaskPriority';
import { TaskStatus } from './TaskStatus';

export type TaskData = {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
};
