package com.k24.coursegradingmanagementsystem.repository;

import com.k24.coursegradingmanagementsystem.entity.Course;
import com.k24.coursegradingmanagementsystem.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    boolean existsByCourseCode(String courseCode);

    List<Course> findByLecturerId(Long lecturerId);

    @Query("SELECT c FROM Course c WHERE " +
           "(:keyword IS NULL OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR c.status = :status)")
    Page<Course> searchCourses(@Param("keyword") String keyword,
                               @Param("status") CourseStatus status,
                               Pageable pageable);
}
