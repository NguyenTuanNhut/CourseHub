package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.RegisterStudentRequest;
import com.k24.coursegradingmanagementsystem.dto.response.UserResponse;
import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.exception.ConflictException;
import com.k24.coursegradingmanagementsystem.repository.PasswordResetTokenRepository;
import com.k24.coursegradingmanagementsystem.repository.RefreshTokenRepository;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.security.JwtProvider;
import com.k24.coursegradingmanagementsystem.service.impl.AuthServiceImpl;
import com.k24.coursegradingmanagementsystem.service.token.RevokedTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private RevokedTokenService revokedTokenService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterStudentRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterStudentRequest();
        validRequest.setUsername("student01");
        validRequest.setEmail("student01@example.com");
        validRequest.setPassword("Password@123");
        validRequest.setConfirmPassword("Password@123");
        validRequest.setFullName("Student One");
        validRequest.setPhone("0900000000");
    }

    @Test
    void registerStudent_shouldCreateStudent_whenRequestIsValid() {
        // Given
        when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .username(validRequest.getUsername())
                .email(validRequest.getEmail())
                .passwordHash("encodedPassword")
                .fullName(validRequest.getFullName())
                .phone(validRequest.getPhone())
                .role(Role.STUDENT)
                .isActive(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse response = authService.registerStudent(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("student01", response.getUsername());
        assertEquals("student01@example.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerStudent_shouldThrowConflictException_whenEmailExists() {
        // Given
        when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () -> authService.registerStudent(validRequest));
        verify(userRepository, never()).save(any(User.class));
    }
}
