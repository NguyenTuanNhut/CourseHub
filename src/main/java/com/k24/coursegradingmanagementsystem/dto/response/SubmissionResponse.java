package com.k24.coursegradingmanagementsystem.dto.response;

import com.k24.coursegradingmanagementsystem.enums.SubmissionStatus;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String githubUrl;
    private String reportUrl;
    private String originalFileName;
    private String fileType;
    private SubmissionStatus status;
    private Instant submittedAt;
    private Instant updatedAt;
}
