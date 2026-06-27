package com.k24.coursegradingmanagementsystem.dto.response;

import com.k24.coursegradingmanagementsystem.enums.Role;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
