import { Locator, Page } from '@playwright/test';

export class LanguageSwitcherDropdown {
  readonly dropdown: Locator;
  readonly dropdownValue: Locator;
  readonly items: Locator;

  constructor(page: Page) {
    this.dropdown = page.getByTestId('language-switcher');
    this.dropdownValue = this.dropdown.locator('span.p-dropdown-label');
    this.items = page.locator('.p-dropdown-item');
  }
}
