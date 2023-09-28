package context;

import data.Item;
import data.ItemRequest;
import lombok.Builder;
import lombok.Data;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Builder
@Data
public class ItemContext {
    @Builder.Default
    public String name = randomAlphabetic(12);

    @Builder.Default
    public String amount = "1";

    @Builder.Default
    public String description = randomAlphabetic(12);

    private String id;

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
}
