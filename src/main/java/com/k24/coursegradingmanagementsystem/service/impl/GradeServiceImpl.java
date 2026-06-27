package com.k24.coursegradingmanagementsystem.service.impl;

import com.k24.coursegradingmanagementsystem.dto.request.GradeSubmissionRequest;
import com.k24.coursegradingmanagementsystem.dto.response.GradeResponse;
import com.k24.coursegradingmanagementsystem.entity.Assignment;
import com.k24.coursegradingmanagementsystem.entity.Course;
import com.k24.coursegradingmanagementsystem.entity.Grade;
import com.k24.coursegradingmanagementsystem.entity.Submission;
import com.k24.coursegradingmanagementsystem.enums.SubmissionStatus;
import com.k24.coursegradingmanagementsystem.exception.BusinessRuleException;
import com.k24.coursegradingmanagementsystem.exception.ForbiddenOperationException;
import com.k24.coursegradingmanagementsystem.exception.InvalidGradeException;
import com.k24.coursegradingmanagementsystem.exception.ResourceNotFoundException;
import com.k24.coursegradingmanagementsystem.mapper.GradeMapper;
import com.k24.coursegradingmanagementsystem.repository.AssignmentRepository;
import com.k24.coursegradingmanagementsystem.repository.CourseRepository;
import com.k24.coursegradingmanagementsystem.repository.GradeRepository;
import com.k24.coursegradingmanagementsystem.repository.SubmissionRepository;
import com.k24.coursegradingmanagementsystem.service.GradeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public GradeServiceImpl(GradeRepository gradeRepository,
                            SubmissionRepository submissionRepository,
                            AssignmentRepository assignmentRepository,
                            CourseRepository courseRepository) {
        this.gradeRepository = gradeRepository;
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public GradeResponse gradeSubmission(Long lecturerId, GradeSubmissionRequest request) {
        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + request.getSubmissionId()));

        validateLecturerOwnership(lecturerId, submission.getAssignmentId());

        if (submission.getStatus() == SubmissionStatus.PENDING) {
            throw new BusinessRuleException("Cannot grade a pending submission");
        }

        double score = request.getScore();
        if (score < 0 || score > 100) {
            throw new InvalidGradeException("Score must be between 0 and 100");
        }

        if (gradeRepository.existsBySubmissionId(submission.getId())) {
            throw new BusinessRuleException("Submission has already been graded. Please use the update API to change grades.");
        }

        Grade grade = Grade.builder()
                .submissionId(submission.getId())
                .lecturerId(lecturerId)
                .score(score)
                .feedback(request.getFeedback())
                .build();

        submission.setStatus(SubmissionStatus.GRADED);
        submissionRepository.save(submission);

        return GradeMapper.toResponse(gradeRepository.save(grade));
    }

    @Override
    public GradeResponse updateGrade(Long lecturerId, Long gradeId, GradeSubmissionRequest request) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + gradeId));

        Submission submission = submissionRepository.findById(grade.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated submission not found"));

        validateLecturerOwnership(lecturerId, submission.getAssignmentId());

        double score = request.getScore();
        if (score < 0 || score > 100) {
            throw new InvalidGradeException("Score must be between 0 and 100");
        }

        grade.setScore(score);
        grade.setFeedback(request.getFeedback());

        return GradeMapper.toResponse(gradeRepository.save(grade));
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse getGradeBySubmissionId(Long userId, Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        // Security check: if student, they can only view their own grade
        if (!submission.getStudentId().equals(userId)) {
            // Check if lecturer is assigned to course
            try {
                validateLecturerOwnership(userId, submission.getAssignmentId());
            } catch (ForbiddenOperationException e) {
                throw new ForbiddenOperationException("You are not authorized to view this grade");
            }
        }

        Grade grade = gradeRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found for submission id: " + submissionId));

        return GradeMapper.toResponse(grade);
    }

    private void validateLecturerOwnership(Long lecturerId, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getLecturerId() == null || !course.getLecturerId().equals(lecturerId)) {
            throw new ForbiddenOperationException("You do not own the course associated with this submission");
        }
    }
}
