package step;

import lombok.Getter;
import step.validation.LanguageValidator;
import step.validation.TaskValidator;
import step.validation.TasksValidator;

@Getter
public class ValidationManager {
    public TasksValidator tasks = new TasksValidator();
    public TaskValidator task = new TaskValidator();
    public LanguageValidator language = new LanguageValidator();
}
