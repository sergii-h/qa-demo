import {Task, TaskPriority, TaskStatus} from '@/data/models/task';

export const mockTask: Task = {
  id: 'task-1',
  title: 'Sample task',
  description: 'Task description',
  status: TaskStatus.TODO,
  priority: TaskPriority.MEDIUM,
  createdDate: '2026-06-17T10:00:00.000Z',
  updatedDate: '2026-06-17T12:00:00.000Z',
};
