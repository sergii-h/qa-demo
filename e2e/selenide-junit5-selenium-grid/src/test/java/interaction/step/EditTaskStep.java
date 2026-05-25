package interaction.step;

import com.codeborne.selenide.SelenideElement;
import data.TaskData;
import interaction.page.EditTaskForm;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

public class EditTaskStep {
    EditTaskForm editTaskForm = new EditTaskForm();

    @Step("Set task data")
    public EditTaskStep setTaskData(TaskData taskData) {
        editTaskForm.titleField.clear();
        editTaskForm.titleField.setValue(taskData.getTitle());
        editTaskForm.descriptionField.clear();
        editTaskForm.descriptionField.setValue(taskData.getDescription());
        selectOption(
                editTaskForm.statusDropdown,
                editTaskForm.statusOption(taskData.getStatus()),
                editTaskForm.statusDropdownLabel
        );
        selectOption(
                editTaskForm.priorityDropdown,
                editTaskForm.priorityOption(taskData.getPriority()),
                editTaskForm.priorityDropdownLabel
        );

        return this;
    }

    @Step("Submit 'Edit task' form")
    public TaskTableStep submitForm() {
        editTaskForm.saveButton.click();
        editTaskForm.saveButton.shouldNotBe(visible);
        
        return new TaskTableStep();
    }

    private void selectOption(SelenideElement dropdown, SelenideElement option, SelenideElement label) {
        dropdown.click();
        String selectedText = option.shouldBe(visible).getText();
        option.click();
        label.shouldHave(text(selectedText));
    }
}
