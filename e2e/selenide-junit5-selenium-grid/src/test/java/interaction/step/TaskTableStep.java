package interaction.step;

import interaction.page.CreateTaskForm;
import interaction.page.EditTaskForm;
import interaction.page.InfoTaskModal;
import interaction.page.MainPage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.visible;

public class TaskTableStep {
    MainPage mainPage = new MainPage();
    CreateTaskForm createTaskForm = new CreateTaskForm();
    InfoTaskModal infoTaskModal = new InfoTaskModal();
    EditTaskForm editTaskForm = new EditTaskForm();

    @Step("Get task id by title '{title}'")
    public String getTaskIdByTitle(String title) {
        return mainPage.getTaskIdByTitle(title);
    }

    @Step("Open 'Create task' form")
    public CreateTaskStep openCreateTaskForm() {
        mainPage.createTaskButton.click();
        createTaskForm.createButton.shouldBe(visible);
        return new CreateTaskStep();
    }

    @Step("Open 'Task info' modal for task '{taskId}'")
    public void openTaskInfoForm(String taskId) {
        mainPage.infoButton(taskId).shouldBe(visible).click();
        infoTaskModal.title.shouldBe(visible);
    }

    @Step("Open 'Task edit' form for task '{taskId}'")
    public EditTaskStep openTaskEditForm(String taskId) {
        mainPage.editButton(taskId).shouldBe(visible).click();
        editTaskForm.saveButton.shouldBe(visible);
        return new EditTaskStep();
    }

    @Step("Delete task '{taskId}'")
    public void deleteTask(String taskId) {
        mainPage.deleteButton(taskId).shouldBe(visible).click();
        mainPage.deleteButton(taskId).should(disappear);
    }
}
