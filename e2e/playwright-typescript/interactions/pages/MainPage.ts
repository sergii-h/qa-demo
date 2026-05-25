import { Locator, Page } from '@playwright/test';

export class MainPage {
  readonly taskRows: Locator;
  readonly createTaskButton: Locator;
  readonly languageSwitcher: Locator;
  readonly tableHeaders: Locator;

  constructor(private readonly page: Page) {
    this.taskRows = page.locator('[data-testid^="task-title-"]');
    this.createTaskButton = page.getByTestId('add-task-button');
    this.languageSwitcher = page.getByTestId('language-switcher');
    this.tableHeaders = page.locator('.p-datatable-thead');
  }

  taskRowByTitle(title: string): Locator {
    return this.page.locator('[data-testid^="task-title-"]').filter({ hasText: title });
  }

  taskInfoButtonById(id: string): Locator {
    return this.page.getByTestId(`info-button-${id}`);
  }

  taskEditButtonById(id: string): Locator {
    return this.page.getByTestId(`edit-button-${id}`);
  }

  taskDeleteButtonById(id: string): Locator {
    return this.page.getByTestId(`delete-button-${id}`);
  }

  taskTitleByTitle(title: string): Locator {
    return this.page.locator('[data-testid^="task-title-"]').filter({ hasText: title });
  }

  statusTag(status: string): Locator {
    return this.page.getByTestId(`status-tag-${status}`).first();
  }

  priorityTag(priority: string): Locator {
    return this.page.getByTestId(`priority-tag-${priority}`).first();
  }
}
