package context;

import data.DemoEventMessage;
import data.Item;
import data.ItemRequest;
import io.restassured.response.Response;
import lombok.Builder;
import lombok.Data;

import static org.apache.commons.lang3.RandomStringUtils.secure;

@Builder
@Data
public class ItemTestContext {
    @Builder.Default
    private String name = secure().nextAlphabetic(12);

    @Builder.Default
    private String amount = "1";

    @Builder.Default
    private String description = secure().nextAlphabetic(12);

    private String id;
    private Response response;

    public void setResponse(Response response) {
        this.response = response;
        this.id = response.jsonPath().get("id");
    }

    public Item createItemData() {
        return Item.builder()
                .name(name)
                .description(description)
                .amount(amount)
                .build();
    }

    public ItemRequest createItemRequest() {
        return ItemRequest.builder()
                .name(name)
                .description(description)
                .amount(Long.parseLong(amount))
                .build();
    }

    public DemoEventMessage createExpectedEvent() {
        return DemoEventMessage.builder()
                .id(id)
                .name(name)
                .description(description)
                .amount(Long.parseLong(amount))
                .build();
    }
}
