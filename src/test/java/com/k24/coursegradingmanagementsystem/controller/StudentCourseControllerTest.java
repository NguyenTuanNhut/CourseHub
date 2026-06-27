package com.k24.coursegradingmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k24.coursegradingmanagementsystem.controller.student.StudentCourseController;
import com.k24.coursegradingmanagementsystem.dto.response.EnrollmentResponse;
import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import com.k24.coursegradingmanagementsystem.exception.ConflictException;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.security.CustomUserDetailsService;
import com.k24.coursegradingmanagementsystem.security.JwtProvider;
import com.k24.coursegradingmanagementsystem.service.*;
import com.k24.coursegradingmanagementsystem.service.token.RevokedTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.security.CustomUserDetails;

import com.k24.coursegradingmanagementsystem.security.SecurityConfig;
import com.k24.coursegradingmanagementsystem.security.JwtAuthenticationFilter;
import com.k24.coursegradingmanagementsystem.security.AuthEntryPoint;
import com.k24.coursegradingmanagementsystem.security.CustomAccessDeniedHandler;
import org.springframework.context.annotation.Import;

@WebMvcTest(StudentCourseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, AuthEntryPoint.class, CustomAccessDeniedHandler.class})
public class StudentCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private AssignmentService assignmentService;

    @MockBean
    private SubmissionService submissionService;

    @MockBean
    private GradeService gradeService;

    @MockBean
    private LectureMaterialService lectureMaterialService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private RevokedTokenService revokedTokenService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void enrollCourse_shouldReturn201_whenSuccessful() throws Exception {
        // Given
        User student = User.builder().id(1L).username("student01").role(Role.STUDENT).isActive(true).build();
        CustomUserDetails userDetails = new CustomUserDetails(student);

        EnrollmentResponse enrollmentResponse = EnrollmentResponse.builder()
                .id(1L)
                .courseId(10L)
                .studentId(1L)
                .status(EnrollmentStatus.ENROLLED)
                .build();

        when(enrollmentService.enrollCourse(anyLong(), anyLong())).thenReturn(enrollmentResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/student/courses/10/enrollments")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.courseId").value(10));
    }

    @Test
    void enrollCourse_shouldReturn409_whenAlreadyEnrolled() throws Exception {
        // Given
        User student = User.builder().id(1L).username("student01").role(Role.STUDENT).isActive(true).build();
        CustomUserDetails userDetails = new CustomUserDetails(student);

        when(enrollmentService.enrollCourse(anyLong(), anyLong()))
                .thenThrow(new ConflictException("Student is already enrolled in this course"));

        // When & Then
        mockMvc.perform(post("/api/v1/student/courses/10/enrollments")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void studentApi_shouldReturn401_whenTokenIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/student/enrollments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
