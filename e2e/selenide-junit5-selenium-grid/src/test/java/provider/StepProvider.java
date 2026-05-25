package provider;

import interaction.step.*;
import lombok.Getter;

@Getter
public class StepProvider {
    public TaskTableStep tasks = new TaskTableStep();
    public CreateTaskStep createTask = new CreateTaskStep();
    public EditTaskStep editTask = new EditTaskStep();
    public LanguageSwitcherStep language = new LanguageSwitcherStep();
    public NavigationStep navigation = new NavigationStep();
    public AccessibilityStep accessibility = new AccessibilityStep();
}
