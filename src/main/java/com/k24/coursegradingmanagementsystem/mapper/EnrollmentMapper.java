package com.k24.coursegradingmanagementsystem.mapper;

import com.k24.coursegradingmanagementsystem.dto.response.EnrollmentResponse;
import com.k24.coursegradingmanagementsystem.entity.Enrollment;

public class EnrollmentMapper {

    public static EnrollmentResponse toResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourseId())
                .studentId(enrollment.getStudentId())
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}
