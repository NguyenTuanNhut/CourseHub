package com.k24.coursegradingmanagementsystem.controller.student;

import com.k24.coursegradingmanagementsystem.aspect.LogExecutionTime;
import com.k24.coursegradingmanagementsystem.dto.common.ApiResponse;
import com.k24.coursegradingmanagementsystem.dto.common.PagedResponse;
import com.k24.coursegradingmanagementsystem.dto.request.SubmitAssignmentRequest;
import com.k24.coursegradingmanagementsystem.dto.response.*;
import com.k24.coursegradingmanagementsystem.enums.CourseStatus;
import com.k24.coursegradingmanagementsystem.security.CustomUserDetails;
import com.k24.coursegradingmanagementsystem.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Student Course Controller", description = "Endpoints for Students to browse courses, enroll, download materials, and submit projects")
public class StudentCourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final GradeService gradeService;
    private final LectureMaterialService lectureMaterialService;

    public StudentCourseController(CourseService courseService,
                                   EnrollmentService enrollmentService,
                                   AssignmentService assignmentService,
                                   SubmissionService submissionService,
                                   GradeService gradeService,
                                   LectureMaterialService lectureMaterialService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.gradeService = gradeService;
        this.lectureMaterialService = lectureMaterialService;
    }

    // 1. Course Browsing & Enrollment
    @GetMapping("/courses")
    @LogExecutionTime(action = "STUDENT_GET_AVAILABLE_COURSES")
    @Operation(summary = "Search and list courses available for enrollment")
    public ResponseEntity<ApiResponse<PagedResponse<CourseResponse>>> getAvailableCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("courseCode").ascending());
        Page<CourseResponse> resultPage = courseService.getAllCourses(keyword, CourseStatus.OPEN, pageable);
        return ResponseEntity.ok(ApiResponse.<PagedResponse<CourseResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Available courses retrieved successfully")
                .data(PagedResponse.from(resultPage))
                .build());
    }

    @GetMapping("/courses/{courseId}")
    @LogExecutionTime(action = "STUDENT_GET_COURSE")
    @Operation(summary = "Get Course details by ID")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long courseId) {
        CourseResponse response = courseService.getCourseById(courseId);
        return ResponseEntity.ok(ApiResponse.<CourseResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Course details retrieved")
                .data(response)
                .build());
    }

    @PostMapping("/courses/{courseId}/enrollments")
    @LogExecutionTime(action = "STUDENT_ENROLL_COURSE")
    @Operation(summary = "Enroll in a course")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        EnrollmentResponse response = enrollmentService.enrollCourse(userDetails.getUser().getId(), courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<EnrollmentResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CREATED.value())
                .message("Successfully enrolled in course")
                .data(response)
                .build());
    }

    @GetMapping("/enrollments")
    @LogExecutionTime(action = "STUDENT_GET_ENROLLED_COURSES")
    @Operation(summary = "View enrolled courses")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getEnrolledCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CourseResponse> response = enrollmentService.getEnrolledCourses(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.<List<CourseResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Enrolled courses retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/enrollments/{enrollmentId}")
    @LogExecutionTime(action = "STUDENT_GET_ENROLLMENT")
    @Operation(summary = "Get specific enrollment details")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(@PathVariable Long enrollmentId) {
        EnrollmentResponse response = enrollmentService.getEnrollmentById(enrollmentId);
        return ResponseEntity.ok(ApiResponse.<EnrollmentResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Enrollment details retrieved")
                .data(response)
                .build());
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    @LogExecutionTime(action = "STUDENT_CANCEL_ENROLLMENT")
    @Operation(summary = "Cancel course enrollment")
    public ResponseEntity<ApiResponse<Void>> cancelEnrollment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long enrollmentId) {
        enrollmentService.cancelEnrollment(userDetails.getUser().getId(), enrollmentId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Enrollment cancelled successfully")
                .build());
    }

    // 2. Assignments
    @GetMapping("/courses/{courseId}/assignments")
    @LogExecutionTime(action = "STUDENT_GET_COURSE_ASSIGNMENTS")
    @Operation(summary = "Get all assignments for an enrolled course")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAssignmentsByCourse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        List<AssignmentResponse> response = assignmentService.getAssignmentsByCourse(userDetails.getUser().getId(), courseId);
        return ResponseEntity.ok(ApiResponse.<List<AssignmentResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Assignments retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/assignments/{assignmentId}")
    @LogExecutionTime(action = "STUDENT_GET_ASSIGNMENT")
    @Operation(summary = "Get specific assignment details")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long assignmentId) {
        AssignmentResponse response = assignmentService.getAssignmentById(userDetails.getUser().getId(), assignmentId);
        return ResponseEntity.ok(ApiResponse.<AssignmentResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Assignment retrieved successfully")
                .data(response)
                .build());
    }

    // 3. Submissions
    @PostMapping("/assignments/{assignmentId}/submissions")
    @LogExecutionTime(action = "STUDENT_SUBMIT_ASSIGNMENT_URL")
    @Operation(summary = "Submit a GitHub repository URL for an assignment")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitGithubUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmitAssignmentRequest request) {
        SubmissionResponse response = submissionService.submitGithubUrl(userDetails.getUser().getId(), assignmentId, request);
        return ResponseEntity.ok(ApiResponse.<SubmissionResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("GitHub repository submitted successfully")
                .data(response)
                .build());
    }

    @PostMapping(value = "/submissions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogExecutionTime(action = "STUDENT_SUBMIT_ASSIGNMENT_FILE")
    @Operation(summary = "Upload a report file and/or submit GitHub repository")
    public ResponseEntity<ApiResponse<SubmissionResponse>> uploadReportFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long assignmentId,
            @RequestParam(required = false) String githubUrl,
            @RequestPart("file") MultipartFile file) {
        SubmissionResponse response = submissionService.uploadReportFile(userDetails.getUser().getId(), assignmentId, githubUrl, file);
        return ResponseEntity.ok(ApiResponse.<SubmissionResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Report file uploaded and submitted successfully")
                .data(response)
                .build());
    }

    @GetMapping("/submissions")
    @LogExecutionTime(action = "STUDENT_GET_SUBMISSIONS")
    @Operation(summary = "View all submissions of the authenticated Student")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getMySubmissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SubmissionResponse> response = submissionService.getSubmissionsByCourse(userDetails.getUser().getId(), null);
        return ResponseEntity.ok(ApiResponse.<List<SubmissionResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Submissions retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/submissions/{submissionId}")
    @LogExecutionTime(action = "STUDENT_GET_SUBMISSION")
    @Operation(summary = "Get specific submission details")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long submissionId) {
        SubmissionResponse response = submissionService.getSubmissionById(userDetails.getUser().getId(), submissionId);
        return ResponseEntity.ok(ApiResponse.<SubmissionResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Submission details retrieved")
                .data(response)
                .build());
    }

    // 4. Grades
    @GetMapping("/submissions/{submissionId}/grade")
    @LogExecutionTime(action = "STUDENT_GET_GRADE")
    @Operation(summary = "Get grades and feedback for a submission")
    public ResponseEntity<ApiResponse<GradeResponse>> getGradeBySubmissionId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long submissionId) {
        GradeResponse response = gradeService.getGradeBySubmissionId(userDetails.getUser().getId(), submissionId);
        return ResponseEntity.ok(ApiResponse.<GradeResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Grade and feedback retrieved successfully")
                .data(response)
                .build());
    }

    // 5. Materials
    @GetMapping("/courses/{courseId}/materials")
    @LogExecutionTime(action = "STUDENT_GET_COURSE_MATERIALS")
    @Operation(summary = "Get lecture materials for an enrolled course")
    public ResponseEntity<ApiResponse<List<LectureMaterialResponse>>> getCourseMaterials(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId) {
        List<LectureMaterialResponse> response = lectureMaterialService.getMaterialsByCourse(userDetails.getUser().getId(), courseId);
        return ResponseEntity.ok(ApiResponse.<List<LectureMaterialResponse>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Lecture materials retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/materials/{materialId}")
    @LogExecutionTime(action = "STUDENT_GET_MATERIAL")
    @Operation(summary = "Get details of a lecture material")
    public ResponseEntity<ApiResponse<LectureMaterialResponse>> getMaterialById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long materialId) {
        LectureMaterialResponse response = lectureMaterialService.getMaterialById(userDetails.getUser().getId(), materialId);
        return ResponseEntity.ok(ApiResponse.<LectureMaterialResponse>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Material details retrieved")
                .data(response)
                .build());
    }
}
