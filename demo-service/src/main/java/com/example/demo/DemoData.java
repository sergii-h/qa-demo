package com.example.demo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
class DemoData {
    String id;
    String name;
    String description;
    long amount;
}
