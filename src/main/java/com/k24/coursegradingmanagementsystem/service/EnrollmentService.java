package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.response.CourseResponse;
import com.k24.coursegradingmanagementsystem.dto.response.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enrollCourse(Long studentId, Long courseId);

    List<CourseResponse> getEnrolledCourses(Long studentId);

    List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId);

    EnrollmentResponse getEnrollmentById(Long enrollmentId);

    void cancelEnrollment(Long studentId, Long enrollmentId);
}
