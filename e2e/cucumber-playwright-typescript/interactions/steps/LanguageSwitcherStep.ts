import { expect, Page } from '@playwright/test';
import { step } from '@/decorators/step-decorator';
import { LanguageSwitcherDropdown } from '@/interactions/pages/LanguageSwitcherDropdown';

export class LanguageSwitcherStep {
  private readonly dropdown: LanguageSwitcherDropdown;

  constructor(page: Page) {
    this.dropdown = new LanguageSwitcherDropdown(page);
  }

  @step("Select language '{language}'")
  async selectLanguage(language: string): Promise<void> {
    await this.dropdown.dropdown.click();
    await this.dropdown.items.filter({ hasText: language }).click();
    await expect(this.dropdown.dropdownValue).toHaveText(language);
  }
}
