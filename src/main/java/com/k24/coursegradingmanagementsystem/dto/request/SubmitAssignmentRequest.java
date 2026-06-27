package com.k24.coursegradingmanagementsystem.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAssignmentRequest {

    @Pattern(regexp = "^(https:\\/\\/github\\.com\\/[a-zA-Z0-9_.-]+\\/[a-zA-Z0-9_.-]+)?$",
            message = "GitHub repository URL must be valid (e.g. https://github.com/user/repo)")
    private String githubUrl;
}
