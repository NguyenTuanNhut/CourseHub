package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.GradeSubmissionRequest;
import com.k24.coursegradingmanagementsystem.dto.response.GradeResponse;
import com.k24.coursegradingmanagementsystem.entity.Assignment;
import com.k24.coursegradingmanagementsystem.entity.Course;
import com.k24.coursegradingmanagementsystem.entity.Grade;
import com.k24.coursegradingmanagementsystem.entity.Submission;
import com.k24.coursegradingmanagementsystem.enums.SubmissionStatus;
import com.k24.coursegradingmanagementsystem.exception.BusinessRuleException;
import com.k24.coursegradingmanagementsystem.exception.ForbiddenOperationException;
import com.k24.coursegradingmanagementsystem.exception.InvalidGradeException;
import com.k24.coursegradingmanagementsystem.repository.AssignmentRepository;
import com.k24.coursegradingmanagementsystem.repository.CourseRepository;
import com.k24.coursegradingmanagementsystem.repository.GradeRepository;
import com.k24.coursegradingmanagementsystem.repository.SubmissionRepository;
import com.k24.coursegradingmanagementsystem.service.impl.GradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private Course course;
    private Assignment assignment;
    private Submission submission;
    private GradeSubmissionRequest request;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .id(10L)
                .courseCode("CS101")
                .lecturerId(5L) // Lecturer ID is 5
                .build();

        assignment = Assignment.builder()
                .id(1L)
                .courseId(10L)
                .build();

        submission = Submission.builder()
                .id(25L)
                .assignmentId(1L)
                .studentId(2L)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        request = new GradeSubmissionRequest();
        request.setSubmissionId(25L);
        request.setScore(95.0);
        request.setFeedback("Good work");
    }

    @Test
    void gradeSubmission_shouldSaveGradeAndMarkSubmissionAsGraded() {
        // Given
        when(submissionRepository.findById(25L)).thenReturn(Optional.of(submission));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(gradeRepository.existsBySubmissionId(25L)).thenReturn(false);

        Grade savedGrade = Grade.builder()
                .id(100L)
                .submissionId(25L)
                .lecturerId(5L)
                .score(95.0)
                .feedback("Good work")
                .build();
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);

        // When
        GradeResponse response = gradeService.gradeSubmission(5L, request);

        // Then
        assertNotNull(response);
        assertEquals(25L, response.getSubmissionId());
        assertEquals(5L, response.getLecturerId());
        assertEquals(95.0, response.getScore());
        assertEquals(SubmissionStatus.GRADED, submission.getStatus());
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    void gradeSubmission_shouldThrowInvalidStateException_whenSubmissionIsPending() {
        // Given
        submission.setStatus(SubmissionStatus.PENDING);
        when(submissionRepository.findById(25L)).thenReturn(Optional.of(submission));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        // When & Then
        assertThrows(BusinessRuleException.class, () -> gradeService.gradeSubmission(5L, request));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void gradeSubmission_shouldThrowInvalidGradeException_whenScoreIsInvalid() {
        // Given
        request.setScore(105.0); // Invalid score
        when(submissionRepository.findById(25L)).thenReturn(Optional.of(submission));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        // When & Then
        assertThrows(InvalidGradeException.class, () -> gradeService.gradeSubmission(5L, request));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void gradeSubmission_shouldThrowForbiddenException_whenLecturerDoesNotOwnCourse() {
        // Given
        when(submissionRepository.findById(25L)).thenReturn(Optional.of(submission));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        // When & Then - Lecturer 6 tries to grade but Course belongs to Lecturer 5
        assertThrows(ForbiddenOperationException.class, () -> gradeService.gradeSubmission(6L, request));
        verify(gradeRepository, never()).save(any(Grade.class));
    }
}
