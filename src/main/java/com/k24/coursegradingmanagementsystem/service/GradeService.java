package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.GradeSubmissionRequest;
import com.k24.coursegradingmanagementsystem.dto.response.GradeResponse;

public interface GradeService {

    GradeResponse gradeSubmission(Long lecturerId, GradeSubmissionRequest request);

    GradeResponse updateGrade(Long lecturerId, Long gradeId, GradeSubmissionRequest request);

    GradeResponse getGradeBySubmissionId(Long userId, Long submissionId);
}
