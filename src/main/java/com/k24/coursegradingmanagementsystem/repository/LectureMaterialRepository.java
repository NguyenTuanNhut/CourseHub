package com.k24.coursegradingmanagementsystem.repository;

import com.k24.coursegradingmanagementsystem.entity.LectureMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {

    List<LectureMaterial> findByCourseId(Long courseId);
}
