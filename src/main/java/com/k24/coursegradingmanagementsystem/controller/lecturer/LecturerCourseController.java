package com.k24.coursegradingmanagementsystem.controller.lecturer;

import com.k24.coursegradingmanagementsystem.aspect.LogExecutionTime;
import com.k24.coursegradingmanagementsystem.dto.common.ApiResponse;
import com.k24.coursegradingmanagementsystem.dto.request.CreateAssignmentRequest;
import com.k24.coursegradingmanagementsystem.dto.request.GradeSubmissionRequest;
import com.k24.coursegradingmanagementsystem.dto.response.*;
import com.k24.coursegradingmanagementsystem.enums.AssignmentStatus;
import com.k24.coursegradingmanagementsystem.security.CustomUserDetails;
import com.k24.coursegradingmanagementsystem.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lecturer")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Lecturer Course Controller", description = "Endpoints for Lecturers to manage courses, assignments, submissions, grading, and materials")
public class LecturerCourseController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final GradeService gradeService;
    private final LectureMaterialService lectureMaterialService;

    public LecturerCourseController(CourseService courseService,
                                    AssignmentService assignmentService,
                                    SubmissionService submissionService,
                                    GradeService gradeService,
                                    LectureMaterialService lectureMaterialService) {
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.gradeService = gradeService;
        this.lectureMaterialService = lectureMaterialService;
    }

    // 1. Courses
    @GetMapping("/courses")
    @LogExecutionTime(action = "LECTURER_GET_ASSIGNED_COURSES")
    @Operation(summary = "View courses assigned to the authenticated Lecturer")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAssignedCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CourseResponse> response = courseService.getCoursesByLecturerId(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.<List<CourseResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Assigned courses retrieved successfully")
                .data(response)
                .build());
    }

    // 2. Assignments
    @PostMapping("/courses/{courseId}/assignments")
    @LogExecutionTime(action = "LECTURER_CREATE_ASSIGNMENT")
    @Operation(summary = "Create an assignment for a course")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @Valid @RequestBody CreateAssignmentRequest request) {
        AssignmentResponse response = assignmentService.createAssignment(userDetails.getUser().getId(), courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<AssignmentResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CREATED.value())
                .message("Assignment created successfully")
                .data(response)
                .build());
    }

    @GetMapping("/courses/{courseId}/assignments")
    @LogExecutionTime(action = "LECTURER_GET_COURSE_ASSIGNMENTS")
    @Operation(summary = "Get all assignments for a course")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getCourseAssignments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        List<AssignmentResponse> response = assignmentService.getAssignmentsByCourse(userDetails.getUser().getId(), courseId);
        return ResponseEntity.ok(ApiResponse.<List<AssignmentResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Course assignments retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/assignments/{assignmentId}")
    @LogExecutionTime(action = "LECTURER_GET_ASSIGNMENT")
    @Operation(summary = "Get assignment details by ID")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long assignmentId) {
        AssignmentResponse response = assignmentService.getAssignmentById(userDetails.getUser().getId(), assignmentId);
        return ResponseEntity.ok(ApiResponse.<AssignmentResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Assignment details retrieved successfully")
                .data(response)
                .build());
    }

    @PutMapping("/assignments/{assignmentId}")
    @LogExecutionTime(action = "LECTURER_UPDATE_ASSIGNMENT")
    @Operation(summary = "Update assignment details")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long assignmentId,
            @Valid @RequestBody CreateAssignmentRequest request) {
        AssignmentResponse response = assignmentService.updateAssignment(userDetails.getUser().getId(), assignmentId, request);
        return ResponseEntity.ok(ApiResponse.<AssignmentResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Assignment updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/assignments/{assignmentId}/status")
    @LogExecutionTime(action = "LECTURER_UPDATE_ASSIGNMENT_STATUS")
    @Operation(summary = "Update assignment status")
    public ResponseEntity<ApiResponse<Void>> updateAssignmentStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long assignmentId,
            @RequestParam AssignmentStatus status) {
        assignmentService.updateAssignmentStatus(userDetails.getUser().getId(), assignmentId, status);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Assignment status updated successfully")
                .build());
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @LogExecutionTime(action = "LECTURER_DELETE_ASSIGNMENT")
    @Operation(summary = "Delete an assignment")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long assignmentId) {
        assignmentService.deleteAssignment(userDetails.getUser().getId(), assignmentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.NO_CONTENT.value())
                .message("Assignment deleted successfully")
                .build());
    }

    // 3. Submissions
    @GetMapping("/courses/{courseId}/submissions")
    @LogExecutionTime(action = "LECTURER_GET_COURSE_SUBMISSIONS")
    @Operation(summary = "View all submissions in a course")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getCourseSubmissions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        List<SubmissionResponse> response = submissionService.getSubmissionsByCourse(userDetails.getUser().getId(), courseId);
        return ResponseEntity.ok(ApiResponse.<List<SubmissionResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Course submissions retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @LogExecutionTime(action = "LECTURER_GET_ASSIGNMENT_SUBMISSIONS")
    @Operation(summary = "View all submissions for an assignment")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getAssignmentSubmissions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long assignmentId) {
        List<SubmissionResponse> response = submissionService.getSubmissionsByAssignment(userDetails.getUser().getId(), assignmentId);
        return ResponseEntity.ok(ApiResponse.<List<SubmissionResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Assignment submissions retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/submissions/{submissionId}")
    @LogExecutionTime(action = "LECTURER_GET_SUBMISSION")
    @Operation(summary = "Get specific submission details by ID")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long submissionId) {
        SubmissionResponse response = submissionService.getSubmissionById(userDetails.getUser().getId(), submissionId);
        return ResponseEntity.ok(ApiResponse.<SubmissionResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Submission details retrieved successfully")
                .data(response)
                .build());
    }

    // 4. Grading
    @PostMapping("/grades")
    @LogExecutionTime(action = "LECTURER_GRADE_SUBMISSION")
    @Operation(summary = "Grade a student submission")
    public ResponseEntity<ApiResponse<GradeResponse>> gradeSubmission(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GradeSubmissionRequest request) {
        GradeResponse response = gradeService.gradeSubmission(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.<GradeResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Submission graded successfully")
                .data(response)
                .build());
    }

    @PutMapping("/grades/{gradeId}")
    @LogExecutionTime(action = "LECTURER_UPDATE_GRADE")
    @Operation(summary = "Update an existing grade/feedback")
    public ResponseEntity<ApiResponse<GradeResponse>> updateGrade(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long gradeId,
            @Valid @RequestBody GradeSubmissionRequest request) {
        GradeResponse response = gradeService.updateGrade(userDetails.getUser().getId(), gradeId, request);
        return ResponseEntity.ok(ApiResponse.<GradeResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Grade updated successfully")
                .data(response)
                .build());
    }

    // 5. Lecture Materials
    @PostMapping(value = "/courses/{courseId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogExecutionTime(action = "LECTURER_UPLOAD_MATERIAL")
    @Operation(summary = "Upload lecture material file for a course")
    public ResponseEntity<ApiResponse<LectureMaterialResponse>> uploadMaterial(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestPart("file") MultipartFile file) {
        LectureMaterialResponse response = lectureMaterialService.uploadMaterial(userDetails.getUser().getId(), courseId, title, description, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<LectureMaterialResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CREATED.value())
                .message("Lecture material uploaded successfully")
                .data(response)
                .build());
    }

    @GetMapping("/courses/{courseId}/materials")
    @LogExecutionTime(action = "LECTURER_GET_COURSE_MATERIALS")
    @Operation(summary = "Get list of materials uploaded in a course")
    public ResponseEntity<ApiResponse<List<LectureMaterialResponse>>> getCourseMaterials(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        List<LectureMaterialResponse> response = lectureMaterialService.getMaterialsByCourse(userDetails.getUser().getId(), courseId);
        return ResponseEntity.ok(ApiResponse.<List<LectureMaterialResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Course lecture materials retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/materials/{materialId}")
    @LogExecutionTime(action = "LECTURER_GET_MATERIAL")
    @Operation(summary = "Get specific material details by ID")
    public ResponseEntity<ApiResponse<LectureMaterialResponse>> getMaterialById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long materialId) {
        LectureMaterialResponse response = lectureMaterialService.getMaterialById(userDetails.getUser().getId(), materialId);
        return ResponseEntity.ok(ApiResponse.<LectureMaterialResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Material details retrieved successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/materials/{materialId}")
    @LogExecutionTime(action = "LECTURER_DELETE_MATERIAL")
    @Operation(summary = "Delete lecture material")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long materialId) {
        lectureMaterialService.deleteMaterial(userDetails.getUser().getId(), materialId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.NO_CONTENT.value())
                .message("Material deleted successfully")
                .build());
    }
}
