package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.*;
import com.k24.coursegradingmanagementsystem.dto.response.AuthResponse;
import com.k24.coursegradingmanagementsystem.dto.response.UserResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String authHeader);

    UserResponse registerStudent(RegisterStudentRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
