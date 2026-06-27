package com.k24.coursegradingmanagementsystem.service.impl;

import com.k24.coursegradingmanagementsystem.dto.common.FileUploadResult;
import com.k24.coursegradingmanagementsystem.dto.response.LectureMaterialResponse;
import com.k24.coursegradingmanagementsystem.entity.Course;
import com.k24.coursegradingmanagementsystem.entity.LectureMaterial;
import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.EnrollmentStatus;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.exception.ForbiddenOperationException;
import com.k24.coursegradingmanagementsystem.exception.ResourceNotFoundException;
import com.k24.coursegradingmanagementsystem.mapper.LectureMaterialMapper;
import com.k24.coursegradingmanagementsystem.repository.CourseRepository;
import com.k24.coursegradingmanagementsystem.repository.EnrollmentRepository;
import com.k24.coursegradingmanagementsystem.repository.LectureMaterialRepository;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.service.LectureMaterialService;
import com.k24.coursegradingmanagementsystem.service.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class LectureMaterialServiceImpl implements LectureMaterialService {

    private final LectureMaterialRepository lectureMaterialRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public LectureMaterialServiceImpl(LectureMaterialRepository lectureMaterialRepository,
                                     CourseRepository courseRepository,
                                     EnrollmentRepository enrollmentRepository,
                                     FileStorageService fileStorageService,
                                     UserRepository userRepository) {
        this.lectureMaterialRepository = lectureMaterialRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @Override
    public LectureMaterialResponse uploadMaterial(Long lecturerId, Long courseId, String title, String description, MultipartFile file) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (course.getLecturerId() == null || !course.getLecturerId().equals(lecturerId)) {
            throw new ForbiddenOperationException("You are not authorized to upload materials for this course");
        }

        FileUploadResult uploadResult = fileStorageService.uploadMaterial(file);

        LectureMaterial material = LectureMaterial.builder()
                .courseId(courseId)
                .lecturerId(lecturerId)
                .title(title)
                .description(description)
                .fileUrl(uploadResult.getFileUrl())
                .originalFileName(uploadResult.getOriginalFileName())
                .fileType(uploadResult.getFileType())
                .fileSize(uploadResult.getFileSize())
                .build();

        return LectureMaterialMapper.toResponse(lectureMaterialRepository.save(material));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureMaterialResponse> getMaterialsByCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.STUDENT) {
            if (!enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, userId, EnrollmentStatus.ENROLLED)) {
                throw new ForbiddenOperationException("You must be enrolled in the course to view lecture materials");
            }
        } else if (user.getRole() == Role.LECTURER) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            if (course.getLecturerId() == null || !course.getLecturerId().equals(userId)) {
                throw new ForbiddenOperationException("You do not have access to view materials in this course");
            }
        }

        List<LectureMaterial> materials = lectureMaterialRepository.findByCourseId(courseId);
        return materials.stream()
                .map(LectureMaterialMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LectureMaterialResponse getMaterialById(Long userId, Long materialId) {
        LectureMaterial material = lectureMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture material not found with id: " + materialId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.STUDENT) {
            if (!enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(material.getCourseId(), userId, EnrollmentStatus.ENROLLED)) {
                throw new ForbiddenOperationException("You must be enrolled in the course to access this material");
            }
        } else if (user.getRole() == Role.LECTURER) {
            Course course = courseRepository.findById(material.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            if (course.getLecturerId() == null || !course.getLecturerId().equals(userId)) {
                throw new ForbiddenOperationException("You do not have access to this material");
            }
        }

        return LectureMaterialMapper.toResponse(material);
    }

    @Override
    public void deleteMaterial(Long lecturerId, Long materialId) {
        LectureMaterial material = lectureMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture material not found with id: " + materialId));

        Course course = courseRepository.findById(material.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getLecturerId() == null || !course.getLecturerId().equals(lecturerId)) {
            throw new ForbiddenOperationException("You do not own this course to delete its materials");
        }

        // Delete from local file storage first
        fileStorageService.delete(material.getFileUrl().replace("/api/v1/files/", ""));

        lectureMaterialRepository.delete(material);
    }
}
