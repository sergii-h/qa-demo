package interaction.validation;

import data.TaskData;
import interaction.page.InfoTaskModal;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

public class TaskValidator {
    InfoTaskModal infoTaskModal = new InfoTaskModal();

    @Step("Validate task info data")
    public TaskValidator data(TaskData taskData) {
        infoTaskModal.title.shouldHave(text(taskData.getTitle()));
        infoTaskModal.descriptionField.shouldHave(text(taskData.getDescription()));
        infoTaskModal.statusTag(taskData.getStatus()).shouldBe(visible);
        infoTaskModal.priorityTag(taskData.getPriority()).shouldBe(visible);

        return this;
    }

    @Step("Validate task is marked as valid")
    public void isValid() {
        infoTaskModal.validIcon.shouldBe(visible);
    }
}
