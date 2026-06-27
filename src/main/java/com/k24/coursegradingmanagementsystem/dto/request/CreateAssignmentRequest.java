package com.k24.coursegradingmanagementsystem.dto.request;

import com.k24.coursegradingmanagementsystem.enums.AssignmentStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class CreateAssignmentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    private String description;

    private String instructions;

    @NotNull(message = "Maximum score is required")
    @DecimalMin(value = "0.0", message = "Maximum score must be at least 0")
    @DecimalMax(value = "100.0", message = "Maximum score cannot exceed 100")
    private Double maximumScore;

    @NotNull(message = "Open time is required")
    private Instant openAt;

    @NotNull(message = "Due time is required")
    private Instant dueAt;

    @NotNull(message = "allowLateSubmission is required")
    private Boolean allowLateSubmission;

    private AssignmentStatus status;
}
