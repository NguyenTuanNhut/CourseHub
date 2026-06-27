package com.k24.coursegradingmanagementsystem.service.storage;

import com.k24.coursegradingmanagementsystem.dto.common.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResult uploadSubmission(MultipartFile file);

    FileUploadResult uploadMaterial(MultipartFile file);

    void delete(String publicId);
}
