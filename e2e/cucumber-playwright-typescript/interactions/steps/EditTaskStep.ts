import { expect, Page } from '@playwright/test';
import { step } from '@/decorators/step-decorator';
import { EditTaskForm } from '@/interactions/pages/EditTaskForm';
import { TaskData } from '@/data/TaskData';

export class EditTaskStep {
  private readonly form: EditTaskForm;

  constructor(private readonly page: Page) {
    this.form = new EditTaskForm(page);
  }

  @step('Fill edit task form with {taskData}')
  async fillForm(taskData: Partial<TaskData>): Promise<void> {
    if (taskData.title) {
      await this.form.titleInput.clear();
      await this.form.titleInput.fill(taskData.title);
    }
    if (taskData.description) {
      await this.form.descriptionInput.clear();
      await this.form.descriptionInput.fill(taskData.description);
    }
    if (taskData.status) {
      await this.form.statusDropdown.click();
      await this.form.statusOption(taskData.status).click();
    }
    if (taskData.priority) {
      await this.form.priorityDropdown.click();
      await this.form.priorityOption(taskData.priority).click();
    }
  }

  @step('Submit edit task form')
  async submitForm(): Promise<void> {
    await this.form.submitButton.click();
    await expect(this.form.submitButton).not.toBeVisible();
  }
}
