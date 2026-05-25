import { TaskPriority } from './TaskPriority';
import { TaskStatus } from './TaskStatus';

export type TaskResponse = {
  id: string;
  title: string;
  description: string;
  status: string;
  priority: string;
};
