import { Page, expect } from '@playwright/test';
import { TaskInfoModal } from '@/interactions/pages/TaskInfoModal';
import { TaskData } from '@/data/TaskData';
import { step } from '@/decorators/step-decorator';

export class TaskValidator {
  private readonly modal: TaskInfoModal;

  constructor(page: Page) {
    this.modal = new TaskInfoModal(page);
  }

  @step('Validate task info data: {taskData}')
  async data(task: TaskData): Promise<this> {
    await expect(this.modal.title).toHaveText(task.title);
    await expect(this.modal.description).toHaveText(task.description);
    await expect(this.modal.statusTag(task.status)).toBeVisible();
    await expect(this.modal.priorityTag(task.priority)).toBeVisible();
    return this;
  }

  @step('Validate task is marked as valid')
  async isValid(): Promise<this> {
    await expect(this.modal.validationBadge).toBeVisible();
    return this;
  }

  @step('Validate task is marked as not valid')
  async isNotValid(): Promise<this> {
    await expect(this.modal.validationBadge).not.toBeVisible();
    return this;
  }
}
