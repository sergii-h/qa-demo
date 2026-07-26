import { Locator, Page } from '@playwright/test';
import { TaskPriority } from '@/data/TaskPriority';
import { TaskStatus } from '@/data/TaskStatus';

export class CreateTaskForm {
  readonly titleInput: Locator;
  readonly descriptionInput: Locator;
  readonly statusDropdown: Locator;
  readonly priorityDropdown: Locator;
  readonly submitButton: Locator;

  constructor(private readonly page: Page) {
    this.titleInput = page.getByTestId('create-task-title-input');
    this.descriptionInput = page.locator('#description');
    this.statusDropdown = page.getByTestId('status-dropdown');
    this.priorityDropdown = page.getByTestId('priority-dropdown');
    this.submitButton = page.getByTestId('create-button');
  }

  statusOption(status: TaskStatus): Locator {
    return this.page.getByTestId(`status-dropdown-option-${status}`);
  }

  priorityOption(priority: TaskPriority): Locator {
    return this.page.getByTestId(`priority-dropdown-option-${priority}`);
  }
}
