package com.k24.coursegradingmanagementsystem.dto.response;

import com.k24.coursegradingmanagementsystem.enums.AssignmentStatus;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentResponse {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String instructions;
    private Double maximumScore;
    private Instant openAt;
    private Instant dueAt;
    private Boolean allowLateSubmission;
    private AssignmentStatus status;
    private Long createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
