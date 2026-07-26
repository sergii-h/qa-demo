import { APIRequestContext, Page } from '@playwright/test';
import { AccessibilityStep } from '@/interactions/steps/AccessibilityStep';
import { CreateTaskStep } from '@/interactions/steps/CreateTaskStep';
import { EditTaskStep } from '@/interactions/steps/EditTaskStep';
import { LanguageSwitcherStep } from '@/interactions/steps/LanguageSwitcherStep';
import { NavigationStep } from '@/interactions/steps/NavigationStep';
import { TaskTableStep } from '@/interactions/steps/TaskTableStep';

export class StepProvider {
  readonly navigation: NavigationStep;
  readonly tasks: TaskTableStep;
  readonly createTask: CreateTaskStep;
  readonly editTask: EditTaskStep;
  readonly language: LanguageSwitcherStep;
  readonly accessibility: AccessibilityStep;

  constructor(page: Page, _isMobile: boolean, _request: APIRequestContext) {
    this.navigation = new NavigationStep(page);
    this.tasks = new TaskTableStep(page);
    this.createTask = new CreateTaskStep(page);
    this.editTask = new EditTaskStep(page);
    this.language = new LanguageSwitcherStep(page);
    this.accessibility = new AccessibilityStep(page);
  }
}
