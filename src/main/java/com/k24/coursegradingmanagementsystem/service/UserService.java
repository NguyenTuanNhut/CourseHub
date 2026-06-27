package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.CreateUserRequest;
import com.k24.coursegradingmanagementsystem.dto.request.UpdateUserRequest;
import com.k24.coursegradingmanagementsystem.dto.response.UserResponse;
import com.k24.coursegradingmanagementsystem.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse getUserById(Long userId);

    Page<UserResponse> getAllUsers(String keyword, Role role, Boolean isActive, Pageable pageable);

    void updateUserStatus(Long userId, Boolean isActive);

    void deleteUser(Long userId);
}
