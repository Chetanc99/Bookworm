package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.UserResponse;
import com.bookworm.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User entity) {
        return UserResponse.builder()
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .roleName(entity.getRole().getRoleName())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
