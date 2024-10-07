package com.example.demo.data;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DemoEvent {
    String id;
    String name;
    String description;
    long amount;
}
