package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.RoleChangeRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.PageResponse;
import com.bookworm.backend.dto.response.UserResponse;
import com.bookworm.backend.security.UserPrincipal;
import com.bookworm.backend.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Real admin-management flow (promote/demote, activate/deactivate),
 * replacing AdminBootstrapConfig as the only lever an ADMIN has over other
 * accounts. Every endpoint here is ADMIN-only - see SecurityConfig.
 *
 * Uses @AuthenticationPrincipal instead of a path-variable userId (unlike
 * every other controller in the project) because the acting admin and the
 * target user are two different people here, so there's no single "the
 * path userId" to trust the way UserOwnershipInterceptor does elsewhere.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Promote/demote roles and activate/deactivate accounts (ADMIN only)")
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("userId").ascending());
        Page<UserResponse> result = userAdminService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a single user")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userAdminService.getById(userId)));
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Promote or demote a user by changing their role")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable Long userId, @Valid @RequestBody RoleChangeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Role updated", userAdminService.changeRole(userId, request.getRoleName())));
    }

    @PatchMapping("/{userId}/activate")
    @Operation(summary = "Reactivate a deactivated account")
    public ResponseEntity<ApiResponse<UserResponse>> activate(
            @AuthenticationPrincipal UserPrincipal actingAdmin, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Account activated",
                userAdminService.setActive(actingAdmin.getUserId(), userId, true)));
    }

    @PatchMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate an account (rejected if it's your own or the last ADMIN)")
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(
            @AuthenticationPrincipal UserPrincipal actingAdmin, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Account deactivated",
                userAdminService.setActive(actingAdmin.getUserId(), userId, false)));
    }
}
