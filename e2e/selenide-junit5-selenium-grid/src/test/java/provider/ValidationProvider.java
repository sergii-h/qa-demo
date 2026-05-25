package provider;

import interaction.validation.AccessibilityValidator;
import interaction.validation.LanguageValidator;
import interaction.validation.TaskValidator;
import interaction.validation.TasksValidator;
import lombok.Getter;

@Getter
public class ValidationProvider {
    public TasksValidator tasks = new TasksValidator();
    public TaskValidator task = new TaskValidator();
    public LanguageValidator language = new LanguageValidator();
    public AccessibilityValidator accessibility = new AccessibilityValidator();
}
