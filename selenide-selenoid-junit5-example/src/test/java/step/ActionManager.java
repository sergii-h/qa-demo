package step;

import lombok.Getter;
import step.action.ApiAction;
import step.action.CreateItemAction;
import step.action.EditItemAction;
import step.action.ItemInfoAction;
import step.action.ItemsAction;

@Getter
public class ActionManager {
    public ItemsAction items = new ItemsAction();
    public ApiAction api = new ApiAction();
    public CreateItemAction createItem = new CreateItemAction();
    public EditItemAction editItem = new EditItemAction();
    public ItemInfoAction itemInfo = new ItemInfoAction();
}
