package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.CreateAssignmentRequest;
import com.k24.coursegradingmanagementsystem.dto.response.AssignmentResponse;
import com.k24.coursegradingmanagementsystem.enums.AssignmentStatus;

import java.util.List;

public interface AssignmentService {

    AssignmentResponse createAssignment(Long lecturerId, Long courseId, CreateAssignmentRequest request);

    AssignmentResponse updateAssignment(Long lecturerId, Long assignmentId, CreateAssignmentRequest request);

    AssignmentResponse getAssignmentById(Long userId, Long assignmentId);

    List<AssignmentResponse> getAssignmentsByCourse(Long userId, Long courseId);

    void updateAssignmentStatus(Long lecturerId, Long assignmentId, AssignmentStatus status);

    void deleteAssignment(Long lecturerId, Long assignmentId);
}
