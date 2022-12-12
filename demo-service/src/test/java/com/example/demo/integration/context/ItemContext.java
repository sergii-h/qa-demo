package com.example.demo.integration.context;

import com.example.demo.integration.data.ItemRequest;
import com.example.demo.integration.data.ItemResponse;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ItemContext {
    private String id;

    @Builder.Default
    private String name = "name";

    @Builder.Default
    private String description = "description";

    @Builder.Default
    private long amount = 1;

    public ItemRequest createItemRequest() {
        return ItemRequest.builder()
                .name(name)
                .description(description)
                .amount(amount)
                .build();
    }

    public ItemResponse createExpectedResponse() {
        return ItemResponse.builder()
                .id(id)
                .name(name)
                .description(description)
                .amount(amount)
                .build();
    }
}
