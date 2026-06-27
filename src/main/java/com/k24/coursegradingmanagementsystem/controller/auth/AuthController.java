package com.k24.coursegradingmanagementsystem.controller.auth;

import com.k24.coursegradingmanagementsystem.aspect.LogExecutionTime;
import com.k24.coursegradingmanagementsystem.dto.common.ApiResponse;
import com.k24.coursegradingmanagementsystem.dto.request.*;
import com.k24.coursegradingmanagementsystem.dto.response.AuthResponse;
import com.k24.coursegradingmanagementsystem.dto.response.UserResponse;
import com.k24.coursegradingmanagementsystem.security.CustomUserDetails;
import com.k24.coursegradingmanagementsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Controller", description = "Endpoints for user login, registration, password operations, and token renewal")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @LogExecutionTime(action = "USER_LOGIN")
    @Operation(summary = "Authenticate user and issue access/refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Login successful")
                .data(response)
                .build());
    }

    @PostMapping("/refresh")
    @LogExecutionTime(action = "TOKEN_REFRESH")
    @Operation(summary = "Rotate refresh token and issue new access/refresh token pair")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Tokens successfully refreshed")
                .data(response)
                .build());
    }

    @PostMapping("/logout")
    @LogExecutionTime(action = "USER_LOGOUT")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Logout user and blacklist access/refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Logout successful")
                .build());
    }

    @PostMapping("/register/students")
    @LogExecutionTime(action = "STUDENT_REGISTRATION")
    @Operation(summary = "Public student registration")
    public ResponseEntity<ApiResponse<UserResponse>> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        UserResponse response = authService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<UserResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CREATED.value())
                .message("Student registered successfully")
                .data(response)
                .build());
    }

    @PostMapping("/change-password")
    @LogExecutionTime(action = "CHANGE_PASSWORD")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Change authenticated user password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Password changed successfully. Active refresh tokens revoked.")
                .build());
    }

    @PostMapping("/forgot-password")
    @LogExecutionTime(action = "FORGOT_PASSWORD")
    @Operation(summary = "Request password reset token")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("If the email exists, password reset instructions have been sent.")
                .build());
    }

    @PostMapping("/reset-password")
    @LogExecutionTime(action = "RESET_PASSWORD")
    @Operation(summary = "Reset password using reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Password has been reset successfully.")
                .build());
    }
}
