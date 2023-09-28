package step;

import lombok.Getter;
import step.validation.ItemValidator;
import step.validation.ItemsValidator;

@Getter
public class ValidationManager {
    public ItemsValidator items = new ItemsValidator();
    public ItemValidator item = new ItemValidator();
}
