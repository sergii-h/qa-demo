import { Locator, Page } from '@playwright/test';
import { TaskPriority } from '@/data/TaskPriority';
import { TaskStatus } from '@/data/TaskStatus';

export class TaskInfoModal {
  readonly title: Locator;
  readonly description: Locator;
  readonly validationBadge: Locator;

  constructor(private readonly page: Page) {
    this.title = page.getByTestId('modal-title');
    this.description = page.getByTestId('description');
    this.validationBadge = page.getByTestId('valid');
  }

  statusTag(status: TaskStatus): Locator {
    return this.page.getByTestId('status').getByTestId(`status-tag-${status}`);
  }

  priorityTag(priority: TaskPriority): Locator {
    return this.page.getByTestId('priority').getByTestId(`priority-tag-${priority}`);
  }
}
