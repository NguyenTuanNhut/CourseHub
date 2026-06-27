package com.k24.coursegradingmanagementsystem.mapper;

import com.k24.coursegradingmanagementsystem.dto.response.SubmissionResponse;
import com.k24.coursegradingmanagementsystem.entity.Submission;

public class SubmissionMapper {

    public static SubmissionResponse toResponse(Submission submission) {
        if (submission == null) {
            return null;
        }
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .githubUrl(submission.getGithubUrl())
                .reportUrl(submission.getReportUrl())
                .originalFileName(submission.getOriginalFileName())
                .fileType(submission.getFileType())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
