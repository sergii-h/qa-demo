import { expect, test } from '@playwright/test';
import type { AxeResults } from 'axe-core';
import { step } from '@/decorators/step-decorator';

export class AccessibilityValidator {
  @step('Validate no accessibility violations')
  async hasNoViolations(results: AxeResults): Promise<void> {
    await test.info().attach('Axe accessibility report', {
      body: JSON.stringify(results.violations, null, 2),
      contentType: 'application/json',
    });
    expect(results.violations, 'Expected no accessibility violations').toEqual([]);
  }
}
