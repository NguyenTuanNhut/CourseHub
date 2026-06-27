package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.SubmitAssignmentRequest;
import com.k24.coursegradingmanagementsystem.dto.response.SubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {

    SubmissionResponse submitGithubUrl(Long studentId, Long assignmentId, SubmitAssignmentRequest request);

    SubmissionResponse uploadReportFile(Long studentId, Long assignmentId, String githubUrl, MultipartFile file);

    SubmissionResponse getSubmissionById(Long userId, Long submissionId);

    List<SubmissionResponse> getSubmissionsByAssignment(Long userId, Long assignmentId);

    List<SubmissionResponse> getSubmissionsByCourse(Long userId, Long courseId);

    SubmissionResponse getStudentSubmissionForAssignment(Long studentId, Long assignmentId);
}
