package com.k24.coursegradingmanagementsystem.dto.response;

import com.k24.coursegradingmanagementsystem.enums.CourseStatus;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer credit;
    private Long lecturerId;
    private Integer maximumStudents;
    private LocalDate startDate;
    private LocalDate endDate;
    private CourseStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
