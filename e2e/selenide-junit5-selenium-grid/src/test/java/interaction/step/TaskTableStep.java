package interaction.step;

import interaction.page.CreateTaskForm;
import interaction.page.EditTaskForm;
import interaction.page.InfoTaskModal;
import interaction.page.MainPage;
import io.qameta.allure.Step;

import java.util.Objects;

import static com.codeborne.selenide.Condition.*;

public class TaskTableStep {
    MainPage mainPage = new MainPage();
    CreateTaskForm createTaskForm = new CreateTaskForm();
    InfoTaskModal infoTaskModal = new InfoTaskModal();
    EditTaskForm editTaskForm = new EditTaskForm();

    @Step("Open 'Create task' form")
    public CreateTaskStep openCreateTaskForm() {
        mainPage.createTaskButton.click();
        createTaskForm.createButton.shouldBe(visible);
        return new CreateTaskStep();
    }

    @Step("Open 'Task info' modal for task '{title}'")
    public void openTaskInfoForm(String title) {
        mainPage.infoButton(resolveTaskId(title)).shouldBe(visible).click();
        infoTaskModal.title.shouldBe(visible);
    }

    @Step("Open 'Task edit' form for task '{title}'")
    public EditTaskStep openTaskEditForm(String title) {
        mainPage.editButton(resolveTaskId(title)).shouldBe(visible).click();
        editTaskForm.titleField.shouldHave(value(title));
        
        return new EditTaskStep();
    }

    @Step("Delete task '{title}'")
    public void deleteTask(String title) {
        String id = resolveTaskId(title);
        mainPage.deleteButton(id).shouldBe(visible).click();
        mainPage.deleteButton(id).should(disappear);
    }

    private String resolveTaskId(String title) {
        var taskTitle = mainPage.taskTitleByTitle(title).shouldBe(visible);
        String dataTestId = taskTitle.getAttribute("data-testid");
        return Objects.requireNonNull(dataTestId).replace("task-title-", "");
    }
}
