package com.k24.coursegradingmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k24.coursegradingmanagementsystem.controller.auth.AuthController;
import com.k24.coursegradingmanagementsystem.dto.request.LoginRequest;
import com.k24.coursegradingmanagementsystem.dto.request.RegisterStudentRequest;
import com.k24.coursegradingmanagementsystem.dto.response.AuthResponse;
import com.k24.coursegradingmanagementsystem.dto.response.UserResponse;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.security.CustomUserDetailsService;
import com.k24.coursegradingmanagementsystem.security.JwtProvider;
import com.k24.coursegradingmanagementsystem.service.AuthService;
import com.k24.coursegradingmanagementsystem.service.token.RevokedTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.k24.coursegradingmanagementsystem.security.SecurityConfig;
import com.k24.coursegradingmanagementsystem.security.JwtAuthenticationFilter;
import com.k24.coursegradingmanagementsystem.security.AuthEntryPoint;
import com.k24.coursegradingmanagementsystem.security.CustomAccessDeniedHandler;
import org.springframework.context.annotation.Import;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, AuthEntryPoint.class, CustomAccessDeniedHandler.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private RevokedTokenService revokedTokenService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void login_shouldReturn200AndTokens_whenCredentialsAreValid() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("student01");
        request.setPassword("Password@123");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mockAccessToken")
                .refreshToken("mockRefreshToken")
                .tokenType("Bearer")
                .accessTokenExpiresIn(900L)
                .refreshTokenExpiresIn(604800L)
                .user(UserResponse.builder().id(1L).username("student01").role(Role.STUDENT).build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mockAccessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("mockRefreshToken"))
                .andExpect(jsonPath("$.data.user.username").value("student01"));
    }

    @Test
    void registerStudent_shouldReturn400_whenRequestIsInvalid() throws Exception {
        // Given - Username empty and password too weak
        RegisterStudentRequest request = new RegisterStudentRequest();
        request.setUsername("");
        request.setEmail("invalid-email");
        request.setPassword("123");
        request.setConfirmPassword("123");
        request.setFullName("Student One");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register/students")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }
}
