package com.example.demo;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("items")
@Value
@Builder(toBuilder = true)
class DemoData {
    @Id
    String id;
    String name;
    String description;
    long amount;
}
