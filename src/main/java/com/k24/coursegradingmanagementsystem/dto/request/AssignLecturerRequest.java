package com.k24.coursegradingmanagementsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignLecturerRequest {

    @NotNull(message = "Lecturer ID is required")
    private Long lecturerId;
}
