package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.entity.Course;
import com.k24.coursegradingmanagementsystem.enums.CourseStatus;
import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import com.k24.coursegradingmanagementsystem.exception.BusinessRuleException;
import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.repository.CourseRepository;
import com.k24.coursegradingmanagementsystem.repository.EnrollmentRepository;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .id(1L)
                .courseCode("JAVA101")
                .courseName("Lap Trinh Java Co Ban")
                .maximumStudents(40)
                .status(CourseStatus.OPEN)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(90))
                .build();
    }

    @Test
    void deleteCourse_shouldDelete_whenNoActiveEnrollments() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseIdAndStatus(1L, EnrollmentStatus.ENROLLED)).thenReturn(0L);

        // When
        courseService.deleteCourse(1L);

        // Then
        verify(courseRepository, times(1)).delete(course);
    }

    @Test
    void deleteCourse_shouldThrowBusinessRuleException_whenActiveEnrollmentsExist() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseIdAndStatus(1L, EnrollmentStatus.ENROLLED)).thenReturn(5L);

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> courseService.deleteCourse(1L));
        assertTrue(exception.getMessage().contains("Cannot delete course: Students are already enrolled"));
        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    void assignLecturer_shouldUpdateLecturerId_andKeepOtherData() {
        // Given
        User lecturer = User.builder()
                .id(2L)
                .username("lecturer01")
                .role(Role.LECTURER)
                .isActive(true)
                .build();
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(lecturer));

        // When
        courseService.assignLecturer(1L, 2L);

        // Then
        assertEquals(2L, course.getLecturerId());
        // Verify other fields are not lost
        assertEquals("JAVA101", course.getCourseCode());
        assertEquals("Lap Trinh Java Co Ban", course.getCourseName());
        assertEquals(40, course.getMaximumStudents());
        
        verify(courseRepository, times(1)).save(course);
    }
}
