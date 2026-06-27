package com.k24.coursegradingmanagementsystem.mapper;

import com.k24.coursegradingmanagementsystem.dto.response.GradeResponse;
import com.k24.coursegradingmanagementsystem.entity.Grade;

public class GradeMapper {

    public static GradeResponse toResponse(Grade grade) {
        if (grade == null) {
            return null;
        }
        return GradeResponse.builder()
                .id(grade.getId())
                .submissionId(grade.getSubmissionId())
                .lecturerId(grade.getLecturerId())
                .score(grade.getScore())
                .feedback(grade.getFeedback())
                .gradedAt(grade.getGradedAt())
                .updatedAt(grade.getUpdatedAt())
                .build();
    }
}
