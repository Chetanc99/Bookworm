package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleChangeRequest {

    // Must match an existing USER_ROLES.role_name (e.g. "ADMIN", "CUSTOMER").
    // Free-text on purpose, same reasoning as UserRole itself - new roles can
    // be added via the DB without a redeploy.
    @NotBlank(message = "roleName is required")
    private String roleName;
}
