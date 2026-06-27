package com.k24.coursegradingmanagementsystem.dto.response;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureMaterialResponse {
    private Long id;
    private Long courseId;
    private Long lecturerId;
    private String title;
    private String description;
    private String fileUrl;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private Instant createdAt;
    private Instant updatedAt;
}
