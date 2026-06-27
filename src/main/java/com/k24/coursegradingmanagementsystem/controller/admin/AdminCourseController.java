package com.k24.coursegradingmanagementsystem.controller.admin;

import com.k24.coursegradingmanagementsystem.aspect.LogExecutionTime;
import com.k24.coursegradingmanagementsystem.dto.common.ApiResponse;
import com.k24.coursegradingmanagementsystem.dto.common.PagedResponse;
import com.k24.coursegradingmanagementsystem.dto.request.AssignLecturerRequest;
import com.k24.coursegradingmanagementsystem.dto.request.CreateCourseRequest;
import com.k24.coursegradingmanagementsystem.dto.response.CourseResponse;
import com.k24.coursegradingmanagementsystem.enums.CourseStatus;
import com.k24.coursegradingmanagementsystem.service.CourseService;
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
@RequestMapping("/api/v1/admin/courses")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin Course Controller", description = "Endpoints for administrator Course management (CRUD, status, assign Lecturer)")
public class AdminCourseController {

    private final CourseService courseService;

    public AdminCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @LogExecutionTime(action = "ADMIN_LIST_COURSES")
    @Operation(summary = "Search, filter and paginate courses")
    public ResponseEntity<ApiResponse<PagedResponse<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CourseResponse> resultPage = courseService.getAllCourses(keyword, status, pageable);

        return ResponseEntity.ok(ApiResponse.<PagedResponse<CourseResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Courses retrieved successfully")
                .data(PagedResponse.from(resultPage))
                .build());
    }

    @GetMapping("/{courseId}")
    @LogExecutionTime(action = "ADMIN_GET_COURSE")
    @Operation(summary = "Get Course details by ID")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long courseId) {
        CourseResponse response = courseService.getCourseById(courseId);
        return ResponseEntity.ok(ApiResponse.<CourseResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Course details retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping
    @LogExecutionTime(action = "ADMIN_CREATE_COURSE")
    @Operation(summary = "Create a new course")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<CourseResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CREATED.value())
                .message("Course created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{courseId}")
    @LogExecutionTime(action = "ADMIN_UPDATE_COURSE")
    @Operation(summary = "Replace/Update course details")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateCourseRequest request) {
        CourseResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(ApiResponse.<CourseResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Course updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{courseId}/status")
    @LogExecutionTime(action = "ADMIN_UPDATE_COURSE_STATUS")
    @Operation(summary = "Update Course status (DRAFT, OPEN, IN_PROGRESS, COMPLETED, CLOSED)")
    public ResponseEntity<ApiResponse<Void>> updateCourseStatus(
            @PathVariable Long courseId,
            @RequestParam CourseStatus status) {
        courseService.updateCourseStatus(courseId, status);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Course status updated successfully")
                .build());
    }

    @PatchMapping("/{courseId}/lecturer")
    @LogExecutionTime(action = "ADMIN_ASSIGN_LECTURER")
    @Operation(summary = "Assign a Lecturer to a course")
    public ResponseEntity<ApiResponse<Void>> assignLecturer(
            @PathVariable Long courseId,
            @Valid @RequestBody AssignLecturerRequest request) {
        courseService.assignLecturer(courseId, request.getLecturerId());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Lecturer assigned to course successfully")
                .build());
    }

    @DeleteMapping("/{courseId}")
    @LogExecutionTime(action = "ADMIN_DELETE_COURSE")
    @Operation(summary = "Delete course from database")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.NO_CONTENT.value())
                .message("Course deleted successfully")
                .build());
    }
}
