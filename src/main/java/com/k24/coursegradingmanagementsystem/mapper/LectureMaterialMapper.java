package com.k24.coursegradingmanagementsystem.mapper;

import com.k24.coursegradingmanagementsystem.dto.response.LectureMaterialResponse;
import com.k24.coursegradingmanagementsystem.entity.LectureMaterial;

public class LectureMaterialMapper {

    public static LectureMaterialResponse toResponse(LectureMaterial material) {
        if (material == null) {
            return null;
        }
        return LectureMaterialResponse.builder()
                .id(material.getId())
                .courseId(material.getCourseId())
                .lecturerId(material.getLecturerId())
                .title(material.getTitle())
                .description(material.getDescription())
                .fileUrl(material.getFileUrl())
                .originalFileName(material.getOriginalFileName())
                .fileType(material.getFileType())
                .fileSize(material.getFileSize())
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .build();
    }
}
