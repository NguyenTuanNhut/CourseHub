package com.k24.coursegradingmanagementsystem.service.impl;

import com.k24.coursegradingmanagementsystem.dto.request.CreateAssignmentRequest;
import com.k24.coursegradingmanagementsystem.dto.response.AssignmentResponse;
import com.k24.coursegradingmanagementsystem.entity.Assignment;
import com.k24.coursegradingmanagementsystem.entity.Course;
import com.k24.coursegradingmanagementsystem.enums.AssignmentStatus;
import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.exception.BusinessRuleException;
import com.k24.coursegradingmanagementsystem.exception.ForbiddenOperationException;
import com.k24.coursegradingmanagementsystem.exception.ResourceNotFoundException;
import com.k24.coursegradingmanagementsystem.mapper.AssignmentMapper;
import com.k24.coursegradingmanagementsystem.repository.AssignmentRepository;
import com.k24.coursegradingmanagementsystem.repository.CourseRepository;
import com.k24.coursegradingmanagementsystem.repository.EnrollmentRepository;
import com.k24.coursegradingmanagementsystem.repository.SubmissionRepository;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.service.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 CourseRepository courseRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 SubmissionRepository submissionRepository,
                                 UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AssignmentResponse createAssignment(Long lecturerId, Long courseId, CreateAssignmentRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (course.getLecturerId() == null || !course.getLecturerId().equals(lecturerId)) {
            throw new ForbiddenOperationException("You are not assigned as the Lecturer for this course");
        }

        if (request.getOpenAt().isAfter(request.getDueAt())) {
            throw new BusinessRuleException("Open time must be before due time");
        }

        Assignment assignment = Assignment.builder()
                .courseId(courseId)
                .title(request.getTitle())
                .description(request.getDescription())
                .instructions(request.getInstructions())
                .maximumScore(request.getMaximumScore() != null ? request.getMaximumScore() : 100.0)
                .openAt(request.getOpenAt())
                .dueAt(request.getDueAt())
                .allowLateSubmission(request.getAllowLateSubmission())
                .status(request.getStatus() != null ? request.getStatus() : AssignmentStatus.DRAFT)
                .createdBy(lecturerId)
                .build();

        return AssignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    public AssignmentResponse updateAssignment(Long lecturerId, Long assignmentId, CreateAssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getLecturerId() == null || !course.getLecturerId().equals(lecturerId)) {
            throw new ForbiddenOperationException("You are not authorized to modify assignments in this course");
        }

        if (request.getOpenAt().isAfter(request.getDueAt())) {
            throw new BusinessRuleException("Open time must be before due time");
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setInstructions(request.getInstructions());
        assignment.setMaximumScore(request.getMaximumScore());
        assignment.setOpenAt(request.getOpenAt());
        assignment.setDueAt(request.getDueAt());
        assignment.setAllowLateSubmission(request.getAllowLateSubmission());
        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
        }

        return AssignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long userId, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.STUDENT) {
            // Verify enrollment
            if (!enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(assignment.getCourseId(), userId, EnrollmentStatus.ENROLLED)) {
                throw new ForbiddenOperationException("You must be enrolled in the course to view this assignment");
            }
        } else if (user.getRole() == Role.LECTURER) {
            // Verify assigned course
            Course course = courseRepository.findById(assignment.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            if (course.getLecturerId() == null || !course.getLecturerId().equals(userId)) {
                throw new ForbiddenOperationException("You do not have access to assignments in this course");
            }
        }

        return AssignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.STUDENT) {
            if (!enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, userId, EnrollmentStatus.ENROLLED)) {
                throw new ForbiddenOperationException("You must be enrolled in the course to view assignments");
            }
        } else if (user.getRole() == Role.LECTURER) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            if (course.getLecturerId() == null || !course.getLecturerId().equals(userId)) {
                throw new ForbiddenOperationException("You are not authorized to view assignments in this course");
            }
        }

        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        return assignments.stream()
                .map(AssignmentMapper::toResponse)
                .toList();
    }

    @Override
    public void updateAssignmentStatus(Long lecturerId, Long assignmentId, AssignmentStatus status) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getLecturerId() == null || !course.getLecturerId().equals(lecturerId)) {
            throw new ForbiddenOperationException("You are not authorized to modify this assignment's status");
        }

        assignment.setStatus(status);
        assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long lecturerId, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getLecturerId() == null || !course.getLecturerId().equals(lecturerId)) {
            throw new ForbiddenOperationException("You are not authorized to delete this assignment");
        }

        // Do not permanently delete an Assignment that already has Submissions
        if (submissionRepository.existsByAssignmentId(assignmentId)) {
            throw new BusinessRuleException("Cannot delete assignment because it already contains student submissions. Please set status to CLOSED instead.");
        }

        assignmentRepository.delete(assignment);
    }
}
