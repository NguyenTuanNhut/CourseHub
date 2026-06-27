package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.response.EnrollmentResponse;
import com.k24.coursegradingmanagementsystem.entity.Course;
import com.k24.coursegradingmanagementsystem.entity.Enrollment;
import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.CourseStatus;
import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.exception.ConflictException;
import com.k24.coursegradingmanagementsystem.repository.CourseRepository;
import com.k24.coursegradingmanagementsystem.repository.EnrollmentRepository;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(2L)
                .username("student01")
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        course = Course.builder()
                .id(10L)
                .courseCode("CS101")
                .courseName("Intro to Computer Science")
                .maximumStudents(30)
                .status(CourseStatus.OPEN)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    void enrollCourse_shouldCreateEnrollment_whenCourseIsAvailable() {
        // Given
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(10L, 2L, EnrollmentStatus.ENROLLED)).thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndStatus(10L, EnrollmentStatus.ENROLLED)).thenReturn(5L);
        
        Enrollment enrollment = Enrollment.builder()
                .id(1L)
                .courseId(10L)
                .studentId(2L)
                .status(EnrollmentStatus.ENROLLED)
                .build();
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        // When
        EnrollmentResponse response = enrollmentService.enrollCourse(2L, 10L);

        // Then
        assertNotNull(response);
        assertEquals(10L, response.getCourseId());
        assertEquals(2L, response.getStudentId());
        assertEquals(EnrollmentStatus.ENROLLED, response.getStatus());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void enrollCourse_shouldThrowConflictException_whenAlreadyEnrolled() {
        // Given
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(10L, 2L, EnrollmentStatus.ENROLLED)).thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () -> enrollmentService.enrollCourse(2L, 10L));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }
}
