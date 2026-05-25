import { expect, Page } from '@playwright/test';
import { step } from '@/decorators/step-decorator';
import { MainPage } from '@/interactions/pages/MainPage';
import { CreateTaskForm } from '@/interactions/pages/CreateTaskForm';
import { CreateTaskStep } from '@/interactions/steps/CreateTaskStep';
import { EditTaskStep } from '@/interactions/steps/EditTaskStep';
import { TaskInfoModal } from '@/interactions/pages/TaskInfoModal';
import { EditTaskForm } from '@/interactions/pages/EditTaskForm';

export class TaskTableStep {
  private readonly mainPage: MainPage;
  private readonly createTaskForm: CreateTaskForm;
  private readonly taskInfoModal: TaskInfoModal;
  private readonly editTaskForm: EditTaskForm;
  readonly createTask: CreateTaskStep;
  readonly editTask: EditTaskStep;

  constructor(page: Page) {
    this.mainPage = new MainPage(page);
    this.createTaskForm = new CreateTaskForm(page);
    this.taskInfoModal = new TaskInfoModal(page);
    this.editTaskForm = new EditTaskForm(page);
    this.createTask = new CreateTaskStep(page);
    this.editTask = new EditTaskStep(page);
  }

  @step('Open create task form')
  async openCreateTaskForm(): Promise<void> {
    await this.mainPage.createTaskButton.click();
    await expect(this.createTaskForm.submitButton).toBeVisible();
  }

  @step("Open task info for task '{title}'")
  async openTaskInfoForm(title: string): Promise<void> {
    const id = await this.resolveTaskId(title);
    await this.mainPage.taskInfoButtonById(id).click();
    await expect(this.taskInfoModal.title).toBeVisible();
  }

  @step("Open edit form for task '{title}'")
  async openEditTaskForm(title: string): Promise<void> {
    const id = await this.resolveTaskId(title);
    await this.mainPage.taskEditButtonById(id).click();
    await expect(this.editTaskForm.submitButton).toBeVisible();
  }

  @step("Delete task '{title}'")
  async deleteTask(title: string): Promise<void> {
    const id = await this.resolveTaskId(title);
    const deleteButton = this.mainPage.taskDeleteButtonById(id);
    await deleteButton.click();
    await expect(deleteButton).not.toBeVisible();
  }

  private async resolveTaskId(title: string): Promise<string> {
    const dataTestId = await this.mainPage.taskTitleByTitle(title).getAttribute('data-testid');
    return dataTestId!.replace('task-title-', '');
  }
}
