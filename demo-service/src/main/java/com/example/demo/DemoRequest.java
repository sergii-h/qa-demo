package com.example.demo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DemoRequest {
    @NotNull
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    private Long amount;
}