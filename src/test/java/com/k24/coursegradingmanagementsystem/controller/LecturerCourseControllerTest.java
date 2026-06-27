package com.k24.coursegradingmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k24.coursegradingmanagementsystem.controller.lecturer.LecturerCourseController;
import com.k24.coursegradingmanagementsystem.dto.request.GradeSubmissionRequest;
import com.k24.coursegradingmanagementsystem.dto.response.GradeResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

@WebMvcTest(LecturerCourseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, AuthEntryPoint.class, CustomAccessDeniedHandler.class})
public class LecturerCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

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
    void gradeSubmission_shouldReturn200_whenRequestIsValid() throws Exception {
        // Given
        User lecturer = User.builder().id(2L).username("lecturer01").role(Role.LECTURER).isActive(true).build();
        CustomUserDetails userDetails = new CustomUserDetails(lecturer);

        GradeSubmissionRequest request = new GradeSubmissionRequest();
        request.setSubmissionId(25L);
        request.setScore(95.0);
        request.setFeedback("Good code quality.");

        GradeResponse response = GradeResponse.builder()
                .id(1L)
                .submissionId(25L)
                .score(95.0)
                .feedback("Good code quality.")
                .build();

        when(gradeService.gradeSubmission(anyLong(), any(GradeSubmissionRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/lecturer/grades")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.score").value(95.0))
                .andExpect(jsonPath("$.data.submissionId").value(25));
    }

    @Test
    @WithMockUser(username = "lecturer01", roles = "LECTURER")
    void gradeSubmission_shouldReturn400_whenScoreIsGreaterThan100() throws Exception {
        // Given - Score is 105 (invalid)
        GradeSubmissionRequest request = new GradeSubmissionRequest();
        request.setSubmissionId(25L);
        request.setScore(105.0);
        request.setFeedback("Good code quality.");

        // When & Then
        mockMvc.perform(post("/api/v1/lecturer/grades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.score").exists());
    }

    @Test
    @WithMockUser(username = "student01", roles = "STUDENT")
    void gradeSubmission_shouldReturn403_whenAccessedByStudent() throws Exception {
        // Given
        GradeSubmissionRequest request = new GradeSubmissionRequest();
        request.setSubmissionId(25L);
        request.setScore(95.0);

        // When & Then
        mockMvc.perform(post("/api/v1/lecturer/grades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
