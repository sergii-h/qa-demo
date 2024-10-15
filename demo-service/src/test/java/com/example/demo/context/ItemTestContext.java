package com.example.demo.context;

import com.example.demo.data.DemoEvent;
import com.example.demo.data.DemoRequest;
import com.example.demo.data.ItemRequest;
import com.example.demo.data.ItemResponse;

import io.restassured.response.Response;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ItemTestContext {
    private String id;
    private Response response;

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

    public DemoRequest createDemoRequest() {
        return DemoRequest.builder()
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

    public DemoEvent createExpectedEvent() {
        return DemoEvent.builder()
                .id(id)
                .name(name)
                .description(description)
                .amount(amount)
                .build();
    }

    public void setResponse(Response response) {
        this.response = response;
        this.id = response.jsonPath().get("id");
    }
}
