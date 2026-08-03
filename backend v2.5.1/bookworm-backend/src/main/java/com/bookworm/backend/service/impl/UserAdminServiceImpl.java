package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.UserResponse;
import com.bookworm.backend.entity.User;
import com.bookworm.backend.entity.UserRole;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.UserMapper;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.repository.UserRoleRepository;
import com.bookworm.backend.service.AuditService;
import com.bookworm.backend.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The real admin-management flow the handoff note called for, replacing
 * AdminBootstrapConfig as the only way an ADMIN gets made/unmade.
 * AdminBootstrapConfig is untouched - it still seeds the very first ADMIN
 * on a fresh DB - but from here on, role changes go through this service
 * so they're auditable via normal REST calls instead of env vars + a
 * restart.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminServiceImpl implements UserAdminService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditService auditService;
    private final UserMapper mapper;

    @Override
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public UserResponse getById(Long userId) {
        return mapper.toResponse(requireUser(userId));
    }

    @Override
    @Transactional
    public UserResponse changeRole(Long userId, String roleName) {
        User user = requireUser(userId);
        UserRole targetRole = userRoleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("UserRole", "role_name", roleName));

        boolean demotingLastAdmin = ADMIN_ROLE.equals(user.getRole().getRoleName())
                && !ADMIN_ROLE.equals(targetRole.getRoleName())
                && userRepository.countByRole_RoleName(ADMIN_ROLE) <= 1;
        if (demotingLastAdmin) {
            throw new IllegalArgumentException("Cannot demote the last remaining ADMIN");
        }

        String previousRole = user.getRole().getRoleName();
        user.setRole(targetRole);
        UserResponse response = mapper.toResponse(userRepository.save(user));
        auditService.log("ROLE_CHANGE", "USER", userId,
                previousRole + " -> " + targetRole.getRoleName() + " (" + user.getEmail() + ")");
        return response;
    }

    @Override
    @Transactional
    public UserResponse setActive(Long actingAdminId, Long userId, boolean active) {
        if (!active && actingAdminId.equals(userId)) {
            throw new IllegalArgumentException("Cannot deactivate your own account");
        }

        User user = requireUser(userId);

        boolean deactivatingLastAdmin = !active
                && ADMIN_ROLE.equals(user.getRole().getRoleName())
                && userRepository.countByRole_RoleName(ADMIN_ROLE) <= 1;
        if (deactivatingLastAdmin) {
            throw new IllegalArgumentException("Cannot deactivate the last remaining ADMIN");
        }

        user.setIsActive(active);
        UserResponse response = mapper.toResponse(userRepository.save(user));
        auditService.log(active ? "ACTIVATE" : "DEACTIVATE", "USER", userId, user.getEmail());
        return response;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "user_id", userId));
    }
}
