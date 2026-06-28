import {TFunction} from 'i18next';

import {TaskPriority, TaskStatus} from '@/data/models/task';
import {taskPriorityLabel, taskStatusLabel} from './taskLabels';

const t = ((key: string) => key) as TFunction;

describe('taskLabels', () => {
  it('should return status labels', () => {
    expect(taskStatusLabel(t, TaskStatus.TODO)).toBe('statusTodo');
    expect(taskStatusLabel(t, TaskStatus.IN_PROGRESS)).toBe('statusInProgress');
    expect(taskStatusLabel(t, TaskStatus.DONE)).toBe('statusDone');
  });

  it('should return priority labels', () => {
    expect(taskPriorityLabel(t, TaskPriority.LOW)).toBe('priorityLow');
    expect(taskPriorityLabel(t, TaskPriority.MEDIUM)).toBe('priorityMedium');
    expect(taskPriorityLabel(t, TaskPriority.HIGH)).toBe('priorityHigh');
  });
});
