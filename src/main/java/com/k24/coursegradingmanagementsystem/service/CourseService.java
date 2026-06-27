package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.request.CreateCourseRequest;
import com.k24.coursegradingmanagementsystem.dto.response.CourseResponse;
import com.k24.coursegradingmanagementsystem.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CreateCourseRequest request);

    CourseResponse updateCourse(Long courseId, CreateCourseRequest request);

    CourseResponse getCourseById(Long courseId);

    Page<CourseResponse> getAllCourses(String keyword, CourseStatus status, Pageable pageable);

    void updateCourseStatus(Long courseId, CourseStatus status);

    void assignLecturer(Long courseId, Long lecturerId);

    void deleteCourse(Long courseId);

    List<CourseResponse> getCoursesByLecturerId(Long lecturerId);
}
