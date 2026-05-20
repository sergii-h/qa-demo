package interaction.step;

import com.codeborne.selenide.SelenideElement;
import data.TaskData;
import interaction.page.CreateTaskForm;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

public class CreateTaskStep {
    CreateTaskForm createTaskForm = new CreateTaskForm();

    @Step("Set task data")
    public CreateTaskStep setTaskData(TaskData taskData) {
        createTaskForm.titleField.setValue(taskData.getTitle());
        createTaskForm.descriptionField.setValue(taskData.getDescription());
        selectOption(
                createTaskForm.statusDropdown,
                createTaskForm.statusOption(taskData.getStatus()),
                createTaskForm.statusDropdownLabel
        );
        selectOption(
                createTaskForm.priorityDropdown,
                createTaskForm.priorityOption(taskData.getPriority()),
                createTaskForm.priorityDropdownLabel
        );
        
        return this;
    }

    private void selectOption(SelenideElement dropdown, SelenideElement option, SelenideElement label) {
        dropdown.click();
        String selectedText = option.shouldBe(visible).getText();
        option.click();
        label.shouldHave(text(selectedText));
    }

    @Step("Submit 'Create task' form")
    public void submitForm() {
        createTaskForm.createButton.click();
        createTaskForm.createButton.shouldNotBe(visible);
    }
}
