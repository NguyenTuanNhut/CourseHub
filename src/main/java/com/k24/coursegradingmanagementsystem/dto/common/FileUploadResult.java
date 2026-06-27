package com.k24.coursegradingmanagementsystem.dto.common;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResult {
    private String fileUrl;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String publicId;
}
