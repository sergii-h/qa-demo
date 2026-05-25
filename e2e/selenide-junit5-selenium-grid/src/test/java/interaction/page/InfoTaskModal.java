package interaction.page;

import com.codeborne.selenide.SelenideElement;
import data.TaskPriority;
import data.TaskStatus;

import static com.codeborne.selenide.Selenide.$;

public class InfoTaskModal {
    public SelenideElement title = $("[data-testid='modal-title']");
    public SelenideElement validIcon = $("[data-testid='valid']");
    public SelenideElement descriptionField = $("[data-testid='description']");

    public SelenideElement statusTag(TaskStatus status) {
        return $("[data-testid='status-tag-" + status + "']");
    }

    public SelenideElement priorityTag(TaskPriority priority) {
        return $("[data-testid='priority-tag-" + priority + "']");
    }
}
