package page;

import com.codeborne.selenide.SelenideElement;
import data.TaskPriority;
import data.TaskStatus;

import static com.codeborne.selenide.Selenide.$;

public class CreateTaskForm {
    public SelenideElement createButton = $("[data-testid='create-button']");
    public SelenideElement titleField = $("[data-testid='create-task-title-input']");
    public SelenideElement descriptionField = $("#description");
    public SelenideElement statusDropdown = $("[data-testid='status-dropdown']");
    public SelenideElement statusDropdownLabel = statusDropdown.$(".p-dropdown-label");
    public SelenideElement priorityDropdown = $("[data-testid='priority-dropdown']");
    public SelenideElement priorityDropdownLabel = priorityDropdown.$(".p-dropdown-label");

    public SelenideElement statusOption(TaskStatus taskStatus) {
        return $("[data-testid='status-dropdown-option-" + taskStatus + "']");
    }

    public SelenideElement priorityOption(TaskPriority taskPriority) {
        return $("[data-testid='priority-dropdown-option-" + taskPriority + "']");
    }
}
