package com.k24.coursegradingmanagementsystem.service.impl;

import com.k24.coursegradingmanagementsystem.dto.request.CreateUserRequest;
import com.k24.coursegradingmanagementsystem.dto.request.UpdateUserRequest;
import com.k24.coursegradingmanagementsystem.dto.response.UserResponse;
import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.exception.ConflictException;
import com.k24.coursegradingmanagementsystem.exception.ResourceNotFoundException;
import com.k24.coursegradingmanagementsystem.mapper.UserMapper;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank() && userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone number is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            userRepository.findByPhone(request.getPhone()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new ConflictException("Phone number is already registered");
                }
            });
        }

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive());

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String keyword, Role role, Boolean isActive, Pageable pageable) {
        Page<User> usersPage = userRepository.searchUsers(keyword, role, isActive, pageable);
        return usersPage.map(UserMapper::toResponse);
    }

    @Override
    public void updateUserStatus(Long userId, Boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setIsActive(isActive);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }
}
