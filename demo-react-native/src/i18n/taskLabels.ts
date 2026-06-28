import {TFunction} from 'i18next';

import {TaskPriority, TaskStatus} from '@/data/models/task';

export function taskStatusLabel(t: TFunction, status: TaskStatus): string {
  switch (status) {
    case TaskStatus.TODO:
      return t('statusTodo');
    case TaskStatus.IN_PROGRESS:
      return t('statusInProgress');
    case TaskStatus.DONE:
      return t('statusDone');
  }
}

export function taskPriorityLabel(t: TFunction, priority: TaskPriority): string {
  switch (priority) {
    case TaskPriority.LOW:
      return t('priorityLow');
    case TaskPriority.MEDIUM:
      return t('priorityMedium');
    case TaskPriority.HIGH:
      return t('priorityHigh');
  }
}
