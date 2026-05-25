import { expect, Page } from '@playwright/test';
import { MainPage } from '@/interactions/pages/MainPage';
import { step } from '@/decorators/step-decorator';

export class LanguageValidator {
  private readonly mainPage: MainPage;

  constructor(page: Page) {
    this.mainPage = new MainPage(page);
  }

  @step('Validate UI is in Spanish')
  async uiIsInSpanish(): Promise<void> {
    await expect(this.mainPage.createTaskButton).toHaveText('Crear tarea');
    await expect(this.mainPage.tableHeaders).toContainText('Título');
    await expect(this.mainPage.tableHeaders).toContainText('Estado');
    await expect(this.mainPage.tableHeaders).toContainText('Prioridad');
  }

  @step('Validate status tag for {status} shows {expectedText}')
  async statusTagShowsText(status: string, expectedText: string): Promise<void> {
    await expect(this.mainPage.statusTag(status)).toHaveText(expectedText);
  }

  @step('Validate priority tag for {priority} shows {expectedText}')
  async priorityTagShowsText(priority: string, expectedText: string): Promise<void> {
    await expect(this.mainPage.priorityTag(priority)).toHaveText(expectedText);
  }
}
