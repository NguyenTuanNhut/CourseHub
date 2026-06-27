package com.k24.coursegradingmanagementsystem.repository;

import com.k24.coursegradingmanagementsystem.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findBySubmissionId(Long submissionId);

    boolean existsBySubmissionId(Long submissionId);
}
