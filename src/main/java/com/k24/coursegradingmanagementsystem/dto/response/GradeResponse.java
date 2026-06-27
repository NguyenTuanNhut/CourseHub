package com.k24.coursegradingmanagementsystem.dto.response;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeResponse {
    private Long id;
    private Long submissionId;
    private Long lecturerId;
    private Double score;
    private String feedback;
    private Instant gradedAt;
    private Instant updatedAt;
}
