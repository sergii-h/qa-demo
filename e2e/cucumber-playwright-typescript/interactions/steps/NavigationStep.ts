import { Page } from '@playwright/test';
import { step } from '@/decorators/step-decorator';

export class NavigationStep {
  constructor(private readonly page: Page) {}

  @step('Navigate to main page')
  async openMainPage(): Promise<void> {
    await this.page.goto('/');
  }

  @step('Refresh page')
  async refresh(): Promise<void> {
    await this.page.reload();
  }
}
