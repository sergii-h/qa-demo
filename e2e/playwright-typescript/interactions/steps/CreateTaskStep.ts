import { expect, Page } from '@playwright/test';
import { step } from '@/decorators/step-decorator';
import { CreateTaskForm } from '@/interactions/pages/CreateTaskForm';
import { TaskData } from '@/data/TaskData';

export class CreateTaskStep {
  private readonly form: CreateTaskForm;

  constructor(page: Page) {
    this.form = new CreateTaskForm(page);
  }

  @step('Fill create task form with {taskData}')
  async fillForm(taskData: TaskData): Promise<void> {
    await this.form.titleInput.fill(taskData.title);
    await this.form.descriptionInput.fill(taskData.description);
    await this.form.statusDropdown.click();
    await this.form.statusOption(taskData.status).click();
    await this.form.priorityDropdown.click();
    await this.form.priorityOption(taskData.priority).click();
  }

  @step('Submit create task form')
  async submitForm(): Promise<void> {
    await this.form.submitButton.click();
    await expect(this.form.submitButton).not.toBeVisible();
  }
}
