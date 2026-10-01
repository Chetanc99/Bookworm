package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserAdminService {

    Page<UserResponse> getAll(Pageable pageable);

    UserResponse getById(Long userId);

    /**
     * Changes a user's role (promote/demote). Rejects if roleName doesn't
     * match an existing USER_ROLES row, or if this would demote the last
     * remaining ADMIN.
     */
    UserResponse changeRole(Long userId, String roleName);

    /** Activates or deactivates a login. Rejects self-deactivation. */
    UserResponse setActive(Long actingAdminId, Long userId, boolean active);
}
