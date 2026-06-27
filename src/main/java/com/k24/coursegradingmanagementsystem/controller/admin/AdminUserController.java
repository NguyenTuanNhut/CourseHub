package com.k24.coursegradingmanagementsystem.controller.admin;

import com.k24.coursegradingmanagementsystem.aspect.LogExecutionTime;
import com.k24.coursegradingmanagementsystem.dto.common.ApiResponse;
import com.k24.coursegradingmanagementsystem.dto.common.PagedResponse;
import com.k24.coursegradingmanagementsystem.dto.request.CreateUserRequest;
import com.k24.coursegradingmanagementsystem.dto.request.UpdateUserRequest;
import com.k24.coursegradingmanagementsystem.dto.response.UserResponse;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/users")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin User Controller", description = "Endpoints for administrator user management (CRUD, status)")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @LogExecutionTime(action = "ADMIN_LIST_USERS")
    @Operation(summary = "Search, filter and paginate system users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> resultPage = userService.getAllUsers(keyword, role, isActive, pageable);

        return ResponseEntity.ok(ApiResponse.<PagedResponse<UserResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Users retrieved successfully")
                .data(PagedResponse.from(resultPage))
                .build());
    }

    @GetMapping("/{userId}")
    @LogExecutionTime(action = "ADMIN_GET_USER")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("User details retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping
    @LogExecutionTime(action = "ADMIN_CREATE_USER")
    @Operation(summary = "Create user account (Lecturer, Admin, Student)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<UserResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CREATED.value())
                .message("User account created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{userId}")
    @LogExecutionTime(action = "ADMIN_UPDATE_USER")
    @Operation(summary = "Replace/Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("User updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{userId}/status")
    @LogExecutionTime(action = "ADMIN_UPDATE_USER_STATUS")
    @Operation(summary = "Toggle user active status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Boolean isActive) {
        userService.updateUserStatus(userId, isActive);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("User status updated successfully")
                .build());
    }

    @DeleteMapping("/{userId}")
    @LogExecutionTime(action = "ADMIN_DELETE_USER")
    @Operation(summary = "Delete user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.NO_CONTENT.value())
                .message("User deleted successfully")
                .build());
    }
}
