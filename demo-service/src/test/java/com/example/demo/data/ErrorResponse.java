package com.example.demo.data;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ErrorResponse {
    private String timestamp;
    private String status;
    private String error;
    private String path;
}
