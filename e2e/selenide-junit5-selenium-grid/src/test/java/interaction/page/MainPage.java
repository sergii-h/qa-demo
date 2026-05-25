package interaction.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import util.SelenideUtil;

import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class MainPage {
    public SelenideElement formLocator = $("#root");
    public SelenideElement tasksTable = $(".tasks-table");
    public SelenideElement tableHeaders = $(".p-datatable-thead");
    public SelenideElement createTaskButton = $("[data-testid='add-task-button']");
    public ElementsCollection taskTitles = $$("[data-testid^='task-title-']");
    public SelenideElement statusTag(String status) {
        return $("[data-testid='status-tag-" + status + "']");
    }

    public SelenideElement priorityTag(String priority) {
        return $("[data-testid='priority-tag-" + priority + "']");
    }

    public SelenideElement infoButton(String taskId) {
        return $("[data-testid='info-button-" + taskId + "']");
    }

    public SelenideElement editButton(String taskId) {
        return $("[data-testid='edit-button-" + taskId + "']");
    }

    public SelenideElement deleteButton(String taskId) {
        return $("[data-testid='delete-button-" + taskId + "']");
    }

    public SelenideElement taskTitleByTitle(String title) {
        return taskTitles.findBy(text(title));
    }

    public List<String> getTaskTitles() {
        formLocator.shouldBe(visible);
        SelenideUtil.waitForEquals(tasksTable::getLocation);
        return taskTitles.texts();
    }
}
