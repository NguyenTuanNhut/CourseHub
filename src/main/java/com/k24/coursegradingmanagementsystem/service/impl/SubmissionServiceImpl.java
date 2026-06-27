package com.k24.coursegradingmanagementsystem.service.impl;

import com.k24.coursegradingmanagementsystem.dto.common.FileUploadResult;
import com.k24.coursegradingmanagementsystem.dto.request.SubmitAssignmentRequest;
import com.k24.coursegradingmanagementsystem.dto.response.SubmissionResponse;
import com.k24.coursegradingmanagementsystem.entity.*;
import com.k24.coursegradingmanagementsystem.enums.AssignmentStatus;
import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.enums.SubmissionStatus;
import com.k24.coursegradingmanagementsystem.exception.BusinessRuleException;
import com.k24.coursegradingmanagementsystem.exception.ConflictException;
import com.k24.coursegradingmanagementsystem.exception.ForbiddenOperationException;
import com.k24.coursegradingmanagementsystem.exception.ResourceNotFoundException;
import com.k24.coursegradingmanagementsystem.mapper.SubmissionMapper;
import com.k24.coursegradingmanagementsystem.repository.*;
import com.k24.coursegradingmanagementsystem.service.SubmissionService;
import com.k24.coursegradingmanagementsystem.service.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository,
                                 AssignmentRepository assignmentRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 CourseRepository courseRepository,
                                 UserRepository userRepository,
                                 FileStorageService fileStorageService) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public SubmissionResponse submitGithubUrl(Long studentId, Long assignmentId, SubmitAssignmentRequest request) {
        Assignment assignment = getAndValidateAssignmentForSubmission(studentId, assignmentId);

        SubmissionStatus newStatus = determineSubmissionStatus(assignment);

        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(null);

        if (submission != null) {
            validateStateTransition(submission.getStatus(), newStatus);
            submission.setGithubUrl(request.getGithubUrl());
            submission.setStatus(newStatus);
            submission.setSubmittedAt(Instant.now());
        } else {
            submission = Submission.builder()
                    .assignmentId(assignmentId)
                    .studentId(studentId)
                    .githubUrl(request.getGithubUrl())
                    .status(newStatus)
                    .submittedAt(Instant.now())
                    .build();
        }

        return SubmissionMapper.toResponse(submissionRepository.save(submission));
    }

    @Override
    public SubmissionResponse uploadReportFile(Long studentId, Long assignmentId, String githubUrl, MultipartFile file) {
        Assignment assignment = getAndValidateAssignmentForSubmission(studentId, assignmentId);

        FileUploadResult uploadResult = fileStorageService.uploadSubmission(file);

        SubmissionStatus newStatus = determineSubmissionStatus(assignment);

        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(null);

        if (submission != null) {
            validateStateTransition(submission.getStatus(), newStatus);
            
            // Delete old file from storage if updating
            if (submission.getReportUrl() != null && submission.getOriginalFileName() != null) {
                fileStorageService.delete(submission.getReportUrl().replace("/api/v1/files/", ""));
            }

            submission.setGithubUrl(githubUrl);
            submission.setReportUrl(uploadResult.getFileUrl());
            submission.setOriginalFileName(uploadResult.getOriginalFileName());
            submission.setFileType(uploadResult.getFileType());
            submission.setStatus(newStatus);
            submission.setSubmittedAt(Instant.now());
        } else {
            submission = Submission.builder()
                    .assignmentId(assignmentId)
                    .studentId(studentId)
                    .githubUrl(githubUrl)
                    .reportUrl(uploadResult.getFileUrl())
                    .originalFileName(uploadResult.getOriginalFileName())
                    .fileType(uploadResult.getFileType())
                    .status(newStatus)
                    .submittedAt(Instant.now())
                    .build();
        }

        return SubmissionMapper.toResponse(submissionRepository.save(submission));
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long userId, Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.STUDENT && !submission.getStudentId().equals(userId)) {
            throw new ForbiddenOperationException("You are not authorized to view this submission");
        }

        if (user.getRole() == Role.LECTURER) {
            Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
            Course course = courseRepository.findById(assignment.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            if (course.getLecturerId() == null || !course.getLecturerId().equals(userId)) {
                throw new ForbiddenOperationException("You do not have access to view submissions in this course");
            }
        }

        return SubmissionMapper.toResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByAssignment(Long userId, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.STUDENT) {
            throw new ForbiddenOperationException("Students cannot list all submissions for an assignment");
        }

        if (user.getRole() == Role.LECTURER && (course.getLecturerId() == null || !course.getLecturerId().equals(userId))) {
            throw new ForbiddenOperationException("You do not own this course to view submissions");
        }

        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(SubmissionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (courseId == null) {
            if (user.getRole() == Role.STUDENT) {
                List<Submission> submissions = submissionRepository.findByStudentId(userId);
                return submissions.stream()
                        .map(SubmissionMapper::toResponse)
                        .toList();
            } else {
                throw new BusinessRuleException("Course ID is required to list submissions");
            }
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (user.getRole() == Role.STUDENT) {
            throw new ForbiddenOperationException("Students cannot view course submissions list");
        }

        if (user.getRole() == Role.LECTURER && (course.getLecturerId() == null || !course.getLecturerId().equals(userId))) {
            throw new ForbiddenOperationException("You do not own this course to view submissions");
        }

        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();

        List<Submission> submissions = submissionRepository.findByAssignmentIdIn(assignmentIds);
        return submissions.stream()
                .map(SubmissionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getStudentSubmissionForAssignment(Long studentId, Long assignmentId) {
        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for student " + studentId + " and assignment " + assignmentId));
        return SubmissionMapper.toResponse(submission);
    }

    private Assignment getAndValidateAssignmentForSubmission(Long studentId, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (!enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(assignment.getCourseId(), studentId, EnrollmentStatus.ENROLLED)) {
            throw new ForbiddenOperationException("Student is not enrolled in this course");
        }

        if (assignment.getStatus() != AssignmentStatus.OPEN) {
            throw new BusinessRuleException("Assignment is closed or in draft status");
        }

        if (Instant.now().isBefore(assignment.getOpenAt())) {
            throw new BusinessRuleException("Assignment is not open for submission yet");
        }

        return assignment;
    }

    private SubmissionStatus determineSubmissionStatus(Assignment assignment) {
        if (Instant.now().isAfter(assignment.getDueAt())) {
            if (!assignment.getAllowLateSubmission()) {
                throw new BusinessRuleException("Late submissions are not permitted for this assignment");
            }
            return SubmissionStatus.LATE;
        }
        return SubmissionStatus.SUBMITTED;
    }

    private void validateStateTransition(SubmissionStatus currentStatus, SubmissionStatus newStatus) {
        if (currentStatus == SubmissionStatus.GRADED) {
            throw new BusinessRuleException("Cannot modify submission because it has already been graded");
        }
    }
}
