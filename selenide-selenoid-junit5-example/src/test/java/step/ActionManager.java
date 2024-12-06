package step;

import lombok.Getter;
import step.action.*;

@Getter
public class ActionManager {
    public ItemsAction items = new ItemsAction();
    public ApiAction api = new ApiAction();
    public CreateItemAction createItem = new CreateItemAction();
    public EditItemAction editItem = new EditItemAction();
    public ItemInfoAction itemInfo = new ItemInfoAction();
    public WireMockAction wiremock = new WireMockAction();
}
