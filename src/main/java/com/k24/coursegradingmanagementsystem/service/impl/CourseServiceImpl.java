package com.k24.coursegradingmanagementsystem.service.impl;

import com.k24.coursegradingmanagementsystem.dto.request.CreateCourseRequest;
import com.k24.coursegradingmanagementsystem.dto.response.CourseResponse;
import com.k24.coursegradingmanagementsystem.entity.Course;
import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.CourseStatus;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.exception.BusinessRuleException;
import com.k24.coursegradingmanagementsystem.exception.ConflictException;
import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import com.k24.coursegradingmanagementsystem.exception.ResourceNotFoundException;
import com.k24.coursegradingmanagementsystem.mapper.CourseMapper;
import com.k24.coursegradingmanagementsystem.repository.CourseRepository;
import com.k24.coursegradingmanagementsystem.repository.EnrollmentRepository;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             UserRepository userRepository,
                             EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new ConflictException("Course code is already registered");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleException("Course start date cannot be after end date");
        }

        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .credit(request.getCredit())
                .maximumStudents(request.getMaximumStudents())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : CourseStatus.DRAFT)
                .build();

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse updateCourse(Long courseId, CreateCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (!course.getCourseCode().equals(request.getCourseCode()) && courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new ConflictException("Course code is already registered");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleException("Course start date cannot be after end date");
        }

        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setCredit(request.getCredit());
        course.setMaximumStudents(request.getMaximumStudents());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return CourseMapper.toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourses(String keyword, CourseStatus status, Pageable pageable) {
        Page<Course> coursesPage = courseRepository.searchCourses(keyword, status, pageable);
        return coursesPage.map(CourseMapper::toResponse);
    }

    @Override
    public void updateCourseStatus(Long courseId, CourseStatus status) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        course.setStatus(status);
        courseRepository.save(course);
    }

    @Override
    public void assignLecturer(Long courseId, Long lecturerId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found with id: " + lecturerId));

        if (lecturer.getRole() != Role.LECTURER) {
            throw new BusinessRuleException("User is not a Lecturer");
        }

        course.setLecturerId(lecturerId);
        courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        long enrollmentCount = enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED);
        if (enrollmentCount > 0) {
            throw new BusinessRuleException("Cannot delete course: Students are already enrolled. You can only update its status instead.");
        }

        courseRepository.delete(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByLecturerId(Long lecturerId) {
        List<Course> courses = courseRepository.findByLecturerId(lecturerId);
        return courses.stream()
                .map(CourseMapper::toResponse)
                .toList();
    }
}
