import { Page } from '@playwright/test';
import { AccessibilityValidator } from '@/interactions/validators/AccessibilityValidator';
import { LanguageValidator } from '@/interactions/validators/LanguageValidator';
import { TaskValidator } from '@/interactions/validators/TaskValidator';
import { TasksValidator } from '@/interactions/validators/TasksValidator';

export class ValidationProvider {
  readonly tasks: TasksValidator;
  readonly task: TaskValidator;
  readonly language: LanguageValidator;
  readonly accessibility: AccessibilityValidator;

  constructor(page: Page, _isMobile: boolean) {
    this.tasks = new TasksValidator(page);
    this.task = new TaskValidator(page);
    this.language = new LanguageValidator(page);
    this.accessibility = new AccessibilityValidator();
  }
}
