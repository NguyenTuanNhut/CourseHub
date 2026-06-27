package com.k24.coursegradingmanagementsystem.service.storage.impl;

import com.k24.coursegradingmanagementsystem.dto.common.FileUploadResult;
import com.k24.coursegradingmanagementsystem.exception.FileStorageException;
import com.k24.coursegradingmanagementsystem.exception.FileValidationException;
import com.k24.coursegradingmanagementsystem.service.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB

    private static final List<String> SUBMISSION_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");
    private static final List<String> SUBMISSION_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final List<String> MATERIAL_EXTENSIONS = Arrays.asList("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "zip");
    private static final List<String> MATERIAL_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip",
            "application/x-zip-compressed"
    );

    private final Path uploadDir;

    public FileStorageServiceImpl(@Value("${app.upload-dir:uploads}") String uploadDirStr) {
        this.uploadDir = Paths.get(uploadDirStr).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new FileStorageException("Could not create local storage upload directory: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResult uploadSubmission(MultipartFile file) {
        validateFile(file, SUBMISSION_EXTENSIONS, SUBMISSION_MIME_TYPES);
        return storeFileLocally(file, "submissions");
    }

    @Override
    public FileUploadResult uploadMaterial(MultipartFile file) {
        validateFile(file, MATERIAL_EXTENSIONS, MATERIAL_MIME_TYPES);
        return storeFileLocally(file, "materials");
    }

    @Override
    public void delete(String publicId) {
        try {
            Path filePath = this.uploadDir.resolve(publicId).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted from local storage: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete file from local storage: {}", publicId, e);
        }
    }

    private void validateFile(MultipartFile file, List<String> allowedExtensions, List<String> allowedMimeTypes) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("Uploaded file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileValidationException("Uploaded file exceeds the maximum allowed size of 15MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new FileValidationException("Filename contains invalid path sequence: " + originalFilename);
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new FileValidationException("Unsupported file extension: " + fileExtension + ". Allowed: " + allowedExtensions);
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            log.warn("MIME type: '{}' did not strictly match, verifying extension: '{}'", contentType, fileExtension);
            // We can warn or enforce, let's enforce or check fallback
        }
    }

    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }

    private FileUploadResult storeFileLocally(MultipartFile file, String subFolder) {
        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename);
            String cleanNameWithoutExtension = originalFilename.substring(0, originalFilename.length() - extension.length() - 1);
            
            // Sanitize file name: remove non-alphanumeric characters, spaces to hyphens
            String sanitizedBase = cleanNameWithoutExtension.replaceAll("[^a-zA-Z0-9-_]", "-");
            String safeStorageName = subFolder + "/" + sanitizedBase + "-" + UUID.randomUUID() + "." + extension;

            Path targetPath = this.uploadDir.resolve(safeStorageName).normalize();
            Files.createDirectories(targetPath.getParent());

            Files.copy(file.getInputStream(), targetPath);

            // Simulating storage url. We use a local URL format: /api/v1/files/{path}
            String fileUrl = "/api/v1/files/" + safeStorageName;

            log.info("File successfully uploaded locally to: {}", targetPath);

            return FileUploadResult.builder()
                    .fileUrl(fileUrl)
                    .originalFileName(originalFilename)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .publicId(safeStorageName)
                    .build();

        } catch (IOException e) {
            log.error("Failed to copy file to local storage", e);
            throw new FileStorageException("Failed to store file on server: " + e.getMessage());
        }
    }
}
