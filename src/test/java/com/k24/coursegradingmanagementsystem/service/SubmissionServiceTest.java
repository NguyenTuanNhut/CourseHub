package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.SubmitAssignmentRequest;
import com.k24.coursegradingmanagementsystem.dto.response.SubmissionResponse;
import com.k24.coursegradingmanagementsystem.entity.Assignment;
import com.k24.coursegradingmanagementsystem.entity.Submission;
import com.k24.coursegradingmanagementsystem.enums.AssignmentStatus;
import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import com.k24.coursegradingmanagementsystem.enums.SubmissionStatus;
import com.k24.coursegradingmanagementsystem.exception.ForbiddenOperationException;
import com.k24.coursegradingmanagementsystem.repository.AssignmentRepository;
import com.k24.coursegradingmanagementsystem.repository.EnrollmentRepository;
import com.k24.coursegradingmanagementsystem.repository.SubmissionRepository;
import com.k24.coursegradingmanagementsystem.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private Assignment assignment;
    private SubmitAssignmentRequest validRequest;

    @BeforeEach
    void setUp() {
        assignment = Assignment.builder()
                .id(1L)
                .courseId(10L)
                .title("Project 1")
                .status(AssignmentStatus.OPEN)
                .openAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .dueAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .allowLateSubmission(false)
                .build();

        validRequest = new SubmitAssignmentRequest();
        validRequest.setGithubUrl("https://github.com/student/project");
    }

    @Test
    void submitAssignment_shouldCreateSubmission_whenStudentIsEnrolled() {
        // Given
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(10L, 2L, EnrollmentStatus.ENROLLED)).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(1L, 2L)).thenReturn(Optional.empty());

        Submission savedSubmission = Submission.builder()
                .id(100L)
                .assignmentId(1L)
                .studentId(2L)
                .githubUrl(validRequest.getGithubUrl())
                .status(SubmissionStatus.SUBMITTED)
                .build();
        when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

        // When
        SubmissionResponse response = submissionService.submitGithubUrl(2L, 1L, validRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getAssignmentId());
        assertEquals(2L, response.getStudentId());
        assertEquals(SubmissionStatus.SUBMITTED, response.getStatus());
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    void submitAssignment_shouldThrowForbiddenException_whenStudentIsNotEnrolled() {
        // Given
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(10L, 2L, EnrollmentStatus.ENROLLED)).thenReturn(false);

        // When & Then
        assertThrows(ForbiddenOperationException.class, () -> submissionService.submitGithubUrl(2L, 1L, validRequest));
        verify(submissionRepository, never()).save(any(Submission.class));
    }
}
