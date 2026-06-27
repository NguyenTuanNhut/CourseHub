package com.k24.coursegradingmanagementsystem.dto.response;

import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {
    private Long id;
    private Long courseId;
    private Long studentId;
    private EnrollmentStatus status;
    private Instant enrolledAt;
    private Instant createdAt;
    private Instant updatedAt;
}
