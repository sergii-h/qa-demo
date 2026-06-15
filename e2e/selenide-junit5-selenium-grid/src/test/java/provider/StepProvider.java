package provider;

import interaction.step.*;
import lombok.Getter;

@Getter
public class StepProvider {
    public TaskTableStep tasks = new TaskTableStep();
    public LanguageSwitcherStep language = new LanguageSwitcherStep();
    public NavigationStep navigation = new NavigationStep();
    public AccessibilityStep accessibility = new AccessibilityStep();
}
