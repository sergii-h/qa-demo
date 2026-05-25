import { Page, expect } from '@playwright/test';
import { MainPage } from '@/interactions/pages/MainPage';
import { step } from '@/decorators/step-decorator';

export class TasksValidator {
  private readonly mainPage: MainPage;

  constructor(page: Page) {
    this.mainPage = new MainPage(page);
  }

  @step('Validate task with title {title} is visible')
  async hasTask(title: string): Promise<this> {
    await expect(this.mainPage.taskRowByTitle(title)).toBeVisible();
    return this;
  }

  @step('Validate task with title {title} is not visible')
  async hasNoTask(title: string): Promise<this> {
    await expect(this.mainPage.taskRowByTitle(title)).not.toBeVisible();
    return this;
  }

  @step('Validate task count is {count}')
  async hasTaskCount(count: number): Promise<this> {
    await expect(this.mainPage.taskRows).toHaveCount(count);
    return this;
  }
}
