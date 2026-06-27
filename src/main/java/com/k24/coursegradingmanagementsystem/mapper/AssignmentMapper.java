package com.k24.coursegradingmanagementsystem.mapper;

import com.k24.coursegradingmanagementsystem.dto.response.AssignmentResponse;
import com.k24.coursegradingmanagementsystem.entity.Assignment;

public class AssignmentMapper {

    public static AssignmentResponse toResponse(Assignment assignment) {
        if (assignment == null) {
            return null;
        }
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourseId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .instructions(assignment.getInstructions())
                .maximumScore(assignment.getMaximumScore())
                .openAt(assignment.getOpenAt())
                .dueAt(assignment.getDueAt())
                .allowLateSubmission(assignment.getAllowLateSubmission())
                .status(assignment.getStatus())
                .createdBy(assignment.getCreatedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
