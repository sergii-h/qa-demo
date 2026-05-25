import { Page } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import type { AxeResults } from 'axe-core';
import { step } from '@/decorators/step-decorator';

export class AccessibilityStep {
  constructor(private readonly page: Page) {}

  @step('Analyze page accessibility with axe')
  async analyze(): Promise<AxeResults> {
    return new AxeBuilder({ page: this.page }).analyze();
  }
}
