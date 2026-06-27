package com.k24.coursegradingmanagementsystem.service;

import com.k24.coursegradingmanagementsystem.dto.response.LectureMaterialResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LectureMaterialService {

    LectureMaterialResponse uploadMaterial(Long lecturerId, Long courseId, String title, String description, MultipartFile file);

    List<LectureMaterialResponse> getMaterialsByCourse(Long userId, Long courseId);

    LectureMaterialResponse getMaterialById(Long userId, Long materialId);

    void deleteMaterial(Long lecturerId, Long materialId);
}
