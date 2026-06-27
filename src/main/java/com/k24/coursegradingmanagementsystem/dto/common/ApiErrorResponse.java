package com.k24.coursegradingmanagementsystem.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;
}
