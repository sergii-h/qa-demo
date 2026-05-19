package step;

import lombok.Getter;
import step.action.*;

@Getter
public class ActionManager {
    public TaskTableAction tasks = new TaskTableAction();
    public ApiAction api = new ApiAction();
    public CreateTaskAction createTask = new CreateTaskAction();
    public EditTaskAction editTask = new EditTaskAction();
    public WireMockAction wiremock = new WireMockAction();
    public LanguageSwitcherAction language = new LanguageSwitcherAction();
    public NavigationAction navigation = new NavigationAction();
}
