package com.example.demo.data;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ItemRequest {
    private String name;
    private String description;
    private long amount;
}
